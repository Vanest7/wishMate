import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
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
  ReservationTarget,
  reservationError,
  validReservation,
} from '../reservation-utils';

@Component({
  selector: 'app-reservation-create-page',
  imports: [RouterLink, MatButtonModule, MatProgressBarModule],
  providers: [ReservationsDataService],
  templateUrl: './reservation-create-page.html',
  styleUrl: './reservations-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReservationCreatePage {
  private readonly data = inject(ReservationsDataService);
  private readonly api = inject(GiftReservationsService);
  private readonly route = inject(ActivatedRoute);
  private readonly session = inject(SessionService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly reload = new Subject<void>();
  readonly target = signal<ReservationTarget | null>(null);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly pending = signal(false);
  readonly completed = signal(false);

  constructor() {
    combineLatest([
      this.route.paramMap,
      toObservable(this.session.userId),
      this.reload.pipe(startWith(undefined)),
    ])
      .pipe(
        tap(() => {
          this.target.set(null);
          this.error.set('');
          this.completed.set(false);
          this.loading.set(true);
        }),
        switchMap(([params, userId]) => {
          const rawIds = ['groupId', 'eventId', 'wishId'].map(
            (key) => params.get(key) ?? '',
          );
          const [groupId, eventId, wishId] = rawIds.map(Number);
          if (
            !positiveId(userId) ||
            !rawIds.every((value) => /^[1-9]\d*$/.test(value)) ||
            ![groupId, eventId, wishId].every(positiveId)
          ) {
            this.error.set('No se puede acceder a este deseo.');
            this.loading.set(false);
            return EMPTY;
          }
          return this.data.getTarget(groupId, eventId, wishId).pipe(
            tap((target) => {
              if (target.wish.userId === userId) {
                this.error.set(
                  'Solo puedes reservar deseos de otras personas.',
                );
              } else this.target.set(target);
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
    if (!this.pending()) this.reload.next();
  }
  reserve(): void {
    const target = this.target();
    const userId = this.session.userId();
    if (
      !target ||
      this.pending() ||
      this.completed() ||
      !canReserve(target, userId)
    )
      return;
    this.pending.set(true);
    this.error.set('');
    this.api
      .createReservation({
        groupId: target.group.id,
        eventId: target.event.id,
        wishId: target.wish.id,
      })
      .pipe(
        finalize(() => this.pending.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (reservation) => {
          if (this.target() !== target || this.session.userId() !== userId)
            return;
          if (
            !validReservation(reservation) ||
            reservation.wishId !== target.wish.id
          ) {
            this.error.set(
              'No hemos podido verificar la respuesta. Consulta Mis reservas antes de volver a intentarlo.',
            );
            return;
          }
          this.completed.set(true);
        },
        error: (error: unknown) => {
          if (this.target() === target && this.session.userId() === userId)
            this.error.set(reservationError(error));
        },
      });
  }
}
