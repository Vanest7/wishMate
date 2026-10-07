import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { WishesService, UpdateWishRequest } from '@wishmate/api-client';
import { finalize, map, Observable } from 'rxjs';
import { SessionService } from '../../../core/auth/session.service';
import {
  canManageWish,
  parsePrice,
  safePurchaseUrl,
  validWish,
  wishError,
} from '../wish-utils';
import { wishesContent } from '../wishes.content';

import {
  WishFormDialogData,
  WishDialogResult,
} from '../models/wish-dialog.model';
export type {
  WishDialogData,
  WishDialogResult,
} from '../models/wish-dialog.model';

@Component({
  selector: 'app-wish-dialog',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './wish-dialog.html',
  styleUrl: './wish-dialog.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WishDialog {
  readonly data = inject<WishFormDialogData>(MAT_DIALOG_DATA);
  private readonly api = inject(WishesService);
  private readonly session = inject(SessionService);
  private readonly ref =
    inject<MatDialogRef<WishDialog, WishDialogResult>>(MatDialogRef);
  private readonly destroyRef = inject(DestroyRef);
  readonly content = wishesContent;
  readonly pending = signal(false);
  readonly error = signal('');
  private readonly initial =
    this.data.mode === 'create' ? undefined : this.data.wish;
  readonly form = new FormGroup({
    name: new FormControl(this.initial?.name ?? '', {
      nonNullable: true,
      validators: [
        (control) =>
          typeof control.value === 'string' && control.value.trim()
            ? null
            : { required: true },
        Validators.maxLength(150),
      ],
    }),
    description: new FormControl(this.initial?.description ?? '', {
      nonNullable: true,
      validators: [
        (control) =>
          typeof control.value === 'string' && control.value.trim()
            ? null
            : { required: true },
        Validators.maxLength(255),
      ],
    }),
    purchaseUrl: new FormControl(this.initial?.purchaseUrl ?? '', {
      nonNullable: true,
      validators: [
        Validators.maxLength(2048),
        (control) =>
          typeof control.value === 'string' &&
          (!control.value.trim() || safePurchaseUrl(control.value))
            ? null
            : { url: true },
      ],
    }),
    estimatedPrice: new FormControl(
      this.initial?.estimatedPrice == null
        ? ''
        : String(this.initial.estimatedPrice).replace('.', ','),
      {
        nonNullable: true,
        validators: [
          (control) =>
            typeof control.value === 'string' &&
            parsePrice(control.value) !== null
              ? null
              : { price: true },
        ],
      },
    ),
  });
  readonly title =
    this.data.mode === 'create'
      ? this.content.createTitle
      : this.content.editTitle;

  cancel(): void {
    if (!this.pending()) this.ref.close();
  }

  submit(): void {
    if (this.pending()) return;
    const data = this.data;
    const userId = this.session.userId();
    if (
      userId === null ||
      data.event.groupId !== data.groupId ||
      data.event.status !== 'ACTIVE' ||
      (data.mode !== 'create' && !canManageWish(data.wish, data.event, userId))
    ) {
      this.error.set(this.content.errors.forbidden);
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const raw = this.form.getRawValue();
    const price = parsePrice(raw.estimatedPrice);
    const body: UpdateWishRequest = {
      name: raw.name.trim(),
      description: raw.description.trim(),
      purchaseUrl: raw.purchaseUrl.trim() || undefined,
      estimatedPrice: price ?? undefined,
    };
    const params = { groupId: data.groupId, eventId: data.event.id };
    const request: Observable<WishDialogResult> = (
      data.mode === 'create'
        ? this.api.createWish({ ...params, body })
        : this.api.updateWish({ ...params, wishId: data.wish.id, body })
    ).pipe(
      map((wish): WishDialogResult => {
        if (
          !validWish(wish, data.event.id) ||
          wish.userId !== userId ||
          (data.mode === 'edit' && wish.id !== data.wish.id)
        )
          throw new Error('Invalid wish response');
        return { mode: data.mode === 'create' ? 'create' : 'edit', wish };
      }),
    );
    this.pending.set(true);
    this.error.set('');
    this.form.disable();
    this.ref.disableClose = true;
    request
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.pending.set(false);
          this.ref.disableClose = false;
          if (this.form.disabled) this.form.enable();
        }),
      )
      .subscribe({
        next: (result) => this.ref.close(result),
        error: (error: unknown) => {
          this.form.enable();
          this.error.set(wishError(error));
          if (error instanceof HttpErrorResponse && error.status === 400) {
            const body: unknown = error.error;
            if (
              body &&
              typeof body === 'object' &&
              'fieldErrors' in body &&
              body.fieldErrors &&
              typeof body.fieldErrors === 'object'
            ) {
              for (const key of [
                'name',
                'description',
                'purchaseUrl',
                'estimatedPrice',
              ] as const) {
                if (key in body.fieldErrors) {
                  this.form.controls[key].setErrors({
                    ...this.form.controls[key].errors,
                    server: true,
                  });
                  this.form.controls[key].markAsTouched();
                }
              }
            }
          }
        },
      });
  }
}
