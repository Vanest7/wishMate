import { GroupMemberResponse, GroupResponse } from '@wishmate/api-client';

export interface GroupSectionContext {
  group: GroupResponse & { id: number };
  members: GroupMemberResponse[];
  memberRemoved: (groupId: number, userId: number) => void;
}
