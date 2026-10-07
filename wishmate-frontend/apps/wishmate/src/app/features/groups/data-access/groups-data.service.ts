import { inject, Injectable } from '@angular/core';
import {
  GroupsService,
  GroupMembersService,
  GroupResponse,
  GroupMemberResponse,
} from '@wishmate/api-client';
import { forkJoin, map, Observable } from 'rxjs';
import { validGroup } from '../group-utils';

export interface GroupDetailData {
  group: GroupResponse & { id: number };
  members: GroupMemberResponse[];
}

@Injectable()
export class GroupsDataService {
  private readonly groups = inject(GroupsService);
  private readonly members = inject(GroupMembersService);
  getDetail(groupId: number): Observable<GroupDetailData> {
    return forkJoin({
      group: this.groups.getGroup({ groupId }),
      members: this.members.getMembers({ groupId }),
    }).pipe(
      map(({ group, members }) => {
        if (!validGroup(group) || group.id !== groupId)
          throw new Error('Invalid group response');
        return { group, members };
      }),
    );
  }
}
