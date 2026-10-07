import { ReservationCard as ReservationCardComponent } from '../ui/reservation-card';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { GiftReservationsService } from '@wishmate/api-client';
import {
  catchError,
  combineLatest,
  EMPTY,
  finalize,
  startWith,
  Subject,
  switchMap,
  tap,
} from 'rxjs';
import { SessionService } from '../../../core/auth/session.service';
import { ReservationsDataService } from '../data-access/reservations-data.service';
import {
  canReserve,
  positiveId,
  ReservationCard,
  reservationError,
} from '../reservation-utils';
import { CancelReservationDialog } from '../ui/cancel-reservation-dialog';

@Component({
  selector: 'app-reservation-list-page',
  imports: [
    RouterLink,
    MatButtonModule,
    MatProgressBarModule,
    ReservationCardComponent,
  ],
  providers: [ReservationsDataService],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './reservation-list-page.html',
  styleUrl: './reservations-page.scss',
})
export class ReservationListPage {
  private readonly data = inject(ReservationsDataService);
  private readonly api = inject(GiftReservationsService);
  private readonly session = inject(SessionService);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);
  private readonly reload = new Subject<void>();
  private dialogRef?: MatDialogRef<CancelReservationDialog, boolean>;
  readonly cards = signal<ReservationCard[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly actionError = signal('');
  readonly success = signal('');
  readonly busy = signal(false);
  readonly cancelling = signal(false);

  constructor() {
    this.destroyRef.onDestroy(() => this.dialogRef?.close());
    combineLatest([
      toObservable(this.session.userId),
      this.reload.pipe(startWith(undefined)),
    ])
      .pipe(
        tap(() => {
          this.dialogRef?.close();
          this.cards.set([]);
          this.loading.set(true);
          this.error.set('');
          this.actionError.set('');
          this.success.set('');
        }),
        switchMap(([userId]) => {
          if (!positiveId(userId)) {
            this.loading.set(false);
            return EMPTY;
          }
          return this.data.getMine(userId).pipe(
            tap((cards) => {
              this.cards.set(cards);
              this.loading.set(false);
            }),
            catchError((error: unknown) => {
              this.error.set(reservationError(error));
              this.loading.set(false);
              return EMPTY;
            }),
          );
        }),
        takeUntilDestroyed(),
      )
      .subscribe();
  }
  retry(): void {
    if (!this.busy()) this.reload.next();
  }
  canCancel(card: ReservationCard): boolean {
    return (
      !!card.target &&
      canReserve(card.target, this.session.userId()) &&
      card.reservation.wishId === card.target.wish.id
    );
  }
  cancel(card: ReservationCard): void {
    if (
      this.busy() ||
      !this.cards().includes(card) ||
      !this.canCancel(card) ||
      !card.target
    )
      return;
    const userId = this.session.userId();
    this.busy.set(true);
    this.actionError.set('');
    this.success.set('');
    const ref = this.dialog.open<CancelReservationDialog, string, boolean>(
      CancelReservationDialog,
      {
        data: card.target.wish.name,
        width: '480px',
        maxWidth: 'calc(100vw - 32px)',
        autoFocus: 'first-tabbable',
      },
    );
    this.dialogRef = ref;
    ref
      .afterClosed()
      .pipe(
        switchMap((confirmed) => {
          this.dialogRef = undefined;
          const target = card.target;
          if (
            !confirmed ||
            !target ||
            this.session.userId() !== userId ||
            !this.cards().includes(card) ||
            !this.canCancel(card)
          )
            return EMPTY;
          this.cancelling.set(true);
          return this.api
            .cancelReservation({
              groupId: target.group.id,
              eventId: target.event.id,
              wishId: target.wish.id,
            })
            .pipe(
              tap(() => {
                if (
                  this.session.userId() !== userId ||
                  !this.cards().includes(card)
                )
                  return;
                this.cards.update((cards) =>
                  cards.filter((item) => item !== card),
                );
                this.success.set('Reserva cancelada.');
              }),
              catchError((error: unknown) => {
                if (
                  this.session.userId() === userId &&
                  this.cards().includes(card)
                )
                  this.actionError.set(reservationError(error));
                return EMPTY;
              }),
            );
        }),
        finalize(() => {
          this.busy.set(false);
          this.cancelling.set(false);
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe();
  }
}
