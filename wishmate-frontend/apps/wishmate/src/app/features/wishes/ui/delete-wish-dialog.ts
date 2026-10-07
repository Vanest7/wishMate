import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { WishesService } from '@wishmate/api-client';
import { finalize } from 'rxjs';
import { SessionService } from '../../../core/auth/session.service';
import {
  DeleteWishDialogData,
  WishDialogResult,
} from '../models/wish-dialog.model';
import { canManageWish, wishError } from '../wish-utils';
import { wishesContent } from '../wishes.content';

@Component({
  selector: 'app-delete-wish-dialog',
  imports: [MatDialogModule, MatButtonModule],
  templateUrl: './delete-wish-dialog.html',
  styleUrl: './delete-wish-dialog.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DeleteWishDialog {
  readonly data = inject<DeleteWishDialogData>(MAT_DIALOG_DATA);
  private readonly api = inject(WishesService);
  private readonly session = inject(SessionService);
  private readonly ref =
    inject<MatDialogRef<DeleteWishDialog, WishDialogResult>>(MatDialogRef);
  private readonly destroyRef = inject(DestroyRef);
  readonly content = wishesContent;
  readonly pending = signal(false);
  readonly error = signal('');

  cancel(): void {
    if (!this.pending()) this.ref.close();
  }

  submit(): void {
    if (this.pending()) return;
    const { groupId, event, wish } = this.data;
    if (
      event.groupId !== groupId ||
      !canManageWish(wish, event, this.session.userId())
    ) {
      this.error.set(this.content.errors.forbidden);
      return;
    }
    this.pending.set(true);
    this.error.set('');
    this.ref.disableClose = true;
    this.api
      .deleteWish({ groupId, eventId: event.id, wishId: wish.id })
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.pending.set(false);
          this.ref.disableClose = false;
        }),
      )
      .subscribe({
        next: () => this.ref.close({ mode: 'delete', wishId: wish.id }),
        error: (error: unknown) => this.error.set(wishError(error)),
      });
  }
}
