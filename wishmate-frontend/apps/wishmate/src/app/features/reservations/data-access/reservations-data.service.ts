import { inject, Injectable } from '@angular/core';
import {
  EventsService,
  GroupsService,
  WishesService,
  GiftReservationsService,
} from '@wishmate/api-client';
import {
  catchError,
  forkJoin,
  from,
  map,
  mergeMap,
  Observable,
  of,
  switchMap,
  toArray,
} from 'rxjs';
import {
  positiveId,
  ReservationCard,
  ReservationTarget,
  reservationDate,
  validReservation,
  validTarget,
} from '../reservation-utils';

@Injectable()
export class ReservationsDataService {
  private readonly reservations = inject(GiftReservationsService);
  private readonly groups = inject(GroupsService);
  private readonly events = inject(EventsService);
  private readonly wishes = inject(WishesService);

  getTarget(
    groupId: number,
    eventId: number,
    wishId: number,
  ): Observable<ReservationTarget> {
    return forkJoin({
      group: this.groups.getGroup({ groupId }),
      event: this.events.getEvent({ groupId, eventId }),
      wish: this.wishes.getWish({ groupId, eventId, wishId }),
    }).pipe(
      map(({ group, event, wish }) => {
        if (
          !validTarget(group, event, wish, groupId, eventId, wishId) ||
          !positiveId(wish.userId)
        )
          throw new Error('Invalid reservation target');
        return {
          group: { ...group, id: groupId, name: group.name as string },
          event: { ...event, id: eventId, groupId, name: event.name as string },
          wish: {
            ...wish,
            id: wishId,
            eventId,
            userId: wish.userId,
            name: wish.name as string,
          },
        };
      }),
    );
  }

  getMine(userId: number): Observable<ReservationCard[]> {
    return this.reservations.getMyReservations().pipe(
      switchMap((reservations) => {
        if (
          !reservations.every(validReservation) ||
          new Set(reservations.map((item) => item.id)).size !==
            reservations.length
        )
          throw new Error('Invalid reservations response');
        if (!reservations.length) return of([]);
        const wanted = new Set(reservations.map((item) => item.wishId));
        // The current reservation DTO has no parent IDs; resolve only visible groups.
        // Keep unresolved reservations visible without inventing names or cancellation paths.
        return this.groups.getMyGroups().pipe(
          switchMap((groups) =>
            from(groups).pipe(
              mergeMap((group) => {
                if (!positiveId(group.id) || !group.name?.trim())
                  return of([] as ReservationTarget[]);
                const groupId = group.id;
                const groupName = group.name;
                return this.events.getEvents({ groupId }).pipe(
                  switchMap((events) =>
                    from(events).pipe(
                      mergeMap((event) => {
                        if (
                          !positiveId(event.id) ||
                          event.groupId !== groupId ||
                          !event.name?.trim()
                        )
                          return of([] as ReservationTarget[]);
                        const eventId = event.id;
                        const eventName = event.name;
                        return this.wishes
                          .getWishesByEvent({ groupId, eventId })
                          .pipe(
                            map((wishes): ReservationTarget[] =>
                              wishes.flatMap((wish) => {
                                if (
                                  !positiveId(wish.id) ||
                                  !wanted.has(wish.id) ||
                                  !positiveId(wish.userId) ||
                                  wish.userId === userId ||
                                  wish.eventId !== eventId ||
                                  !wish.name?.trim()
                                )
                                  return [];
                                return [
                                  {
                                    group: {
                                      ...group,
                                      id: groupId,
                                      name: groupName,
                                    },
                                    event: {
                                      ...event,
                                      id: eventId,
                                      groupId,
                                      name: eventName,
                                    },
                                    wish: {
                                      ...wish,
                                      id: wish.id,
                                      eventId,
                                      userId: wish.userId,
                                      name: wish.name,
                                    },
                                  },
                                ];
                              }),
                            ),
                            catchError(() => of([] as ReservationTarget[])),
                          );
                      }, 3),
                      toArray(),
                      map((targets) => targets.flat()),
                    ),
                  ),
                  catchError(() => of([] as ReservationTarget[])),
                );
              }, 2),
              toArray(),
              map((targets) => targets.flat()),
            ),
          ),
          catchError(() => of([] as ReservationTarget[])),
          map((targets) => {
            const byWish = new Map(
              targets.map((target) => [target.wish.id, target]),
            );
            return reservations.map((reservation) => ({
              reservation,
              target: byWish.get(reservation.wishId) ?? null,
              date: reservationDate(reservation.reservedAt),
            }));
          }),
        );
      }),
    );
  }
}
