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
import { GroupResponse, GroupsService } from '@wishmate/api-client';
import { finalize, map } from 'rxjs';
import { groupsContent } from '../groups.content';
import { groupError, validGroup } from '../group-utils';

export type GroupDialogData =
  | { mode: 'create' }
  | {
      mode: 'rename' | 'delete';
      group: GroupResponse & { id: number };
    };

export type GroupDialogResult =
  | { mode: 'create' | 'rename'; group: GroupResponse }
  | { mode: 'delete' };

@Component({
  selector: 'app-group-dialog',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './group-dialog.html',
  styleUrl: './group-dialog.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
})
export class GroupDialog {
  readonly data = inject<GroupDialogData>(MAT_DIALOG_DATA);

  private readonly ref =
    inject<MatDialogRef<GroupDialog, GroupDialogResult>>(MatDialogRef);
  private readonly api = inject(GroupsService);
  private readonly destroyRef = inject(DestroyRef);

  readonly content = groupsContent;
  readonly pending = signal(false);
  readonly error = signal('');

  readonly name = new FormControl(
    this.data.mode === 'create' ? '' : (this.data.group.name ?? ''),
    {
      nonNullable: true,
      validators: [
        (control) =>
          typeof control.value === 'string' && control.value.trim()
            ? null
            : { required: true },
        Validators.maxLength(100),
      ],
    },
  );

  readonly form = new FormGroup({
    name: this.name,
  });

  readonly title =
    this.data.mode === 'create'
      ? groupsContent.createTitle
      : this.data.mode === 'rename'
        ? groupsContent.renameTitle
        : groupsContent.deleteTitle;

  cancel(): void {
    if (this.pending()) return;

    this.ref.close();
  }

  submit(): void {
    if (this.pending()) return;

    if (this.data.mode !== 'delete' && this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const data = this.data;
    const name = this.name.value.trim();

    this.pending.set(true);
    this.error.set('');
    this.form.disable();
    this.ref.disableClose = true;

    const request =
      data.mode === 'delete'
        ? this.api
            .deleteGroup({ groupId: data.group.id })
            .pipe(map((): GroupDialogResult => ({ mode: 'delete' })))
        : (data.mode === 'create'
            ? this.api.createGroup({ body: { name } })
            : this.api.renameGroup({
                groupId: data.group.id,
                body: { name },
              })
          ).pipe(
            map((group): GroupDialogResult => {
              if (!validGroup(group)) {
                throw new Error('Invalid group response');
              }

              return { mode: data.mode, group };
            }),
          );

    request
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.pending.set(false);
          this.ref.disableClose = false;

          if (this.form.disabled) {
            this.form.enable();
          }
        }),
      )
      .subscribe({
        next: (result) => this.ref.close(result),
        error: (error: unknown) => {
          this.form.enable();
          this.error.set(groupError(error));

          if (error instanceof HttpErrorResponse && error.status === 400) {
            const body: unknown = error.error;

            if (
              body &&
              typeof body === 'object' &&
              'fieldErrors' in body &&
              body.fieldErrors &&
              typeof body.fieldErrors === 'object' &&
              'name' in body.fieldErrors &&
              typeof body.fieldErrors.name === 'string'
            ) {
              this.name.setErrors({
                ...this.name.errors,
                server: true,
              });
              this.name.markAsTouched();
            }
          }
        },
      });
  }
}
