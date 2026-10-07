import { inject, Injectable } from '@angular/core';
import {
  EventsService,
  EventResponse,
  GroupsService,
  GroupResponse,
} from '@wishmate/api-client';
import { map, Observable } from 'rxjs';
import { positiveId } from '../../../shared/util/positive-id';

export type DashboardGroup = GroupResponse & { id: number; name: string };
export type DashboardEvent = EventResponse & { id: number; name: string };

@Injectable()
export class DashboardDataService {
  private readonly groups = inject(GroupsService);
  private readonly events = inject(EventsService);

  getGroups(): Observable<DashboardGroup[]> {
    return this.groups.getMyGroups().pipe(
      map((groups) => {
        if (
          !groups.every(
            (group): group is DashboardGroup =>
              positiveId(group.id) &&
              typeof group.name === 'string' &&
              !!group.name.trim(),
          )
        )
          throw new Error('Invalid groups response');
        return groups;
      }),
    );
  }
  getEvents(groupId: number): Observable<DashboardEvent[]> {
    return this.events.getEvents({ groupId }).pipe(
      map((events) => {
        if (
          !events.every(
            (event): event is DashboardEvent =>
              positiveId(event.id) &&
              event.groupId === groupId &&
              typeof event.name === 'string' &&
              !!event.name.trim(),
          )
        )
          throw new Error('Invalid events response');
        return events;
      }),
    );
  }
}
