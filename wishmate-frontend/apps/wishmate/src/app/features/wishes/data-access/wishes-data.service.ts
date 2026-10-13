import { inject, Injectable } from '@angular/core';
import {
  EventsService,
  GroupsService,
  GroupMembersService,
  WishesService,
} from '@wishmate/api-client';
import { forkJoin, map, Observable, switchMap } from 'rxjs';
import {
  ValidWish,
  validWish,
  validWishEvent,
  WishPageData,
} from '../wish-utils';

@Injectable()
export class WishesDataService {
  private readonly groupsApi = inject(GroupsService);
  private readonly eventsApi = inject(EventsService);
  private readonly membersApi = inject(GroupMembersService);
  private readonly api = inject(WishesService);

  getPage(groupId: number, eventId: number): Observable<WishPageData> {
    return forkJoin({
      group: this.groupsApi.getGroup({ groupId }),
      event: this.eventsApi.getEvent({ groupId, eventId }),
      wishes: this.api.getWishesByEvent({ groupId, eventId }),
      members: this.membersApi.getMembers({ groupId }),
    }).pipe(
      switchMap(({ group, event, wishes, members }) => {
        if (
          !group ||
          group.id !== groupId ||
          typeof group.name !== 'string' ||
          !group.name.trim() ||
          !validWishEvent(event, groupId, eventId) ||
          !wishes.every((wish): wish is ValidWish =>
            validWish(wish, eventId),
          ) ||
          new Set(wishes.map((wish) => wish.id)).size !== wishes.length
        )
          throw new Error('Invalid wish page response');
        return forkJoin(
          wishes.map((wish) =>
            this.api
              .getWishReservationStatus({
                groupId,
                eventId,
                wishId: wish.id,
              })
              .pipe(
                map(
                  (status) =>
                    status === 'RESERVED' || status === 'RESERVED_BY_ME',
                ),
              ),
          ),
        ).pipe(
          map(
            (reserved): WishPageData => ({
              group: { ...group, id: groupId, name: group.name },
              event,
              wishes: wishes.map((wish, index) => ({
                ...wish,
                reserved: reserved[index],
              })),
              members,
            }),
          ),
        );
      }),
    );
  }
}
