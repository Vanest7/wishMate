import { apiErrorMessage } from '../../core/http/api-error';
import { GroupMemberResponse, GroupResponse } from '@wishmate/api-client';
import { groupsContent } from './groups.content';

export function validGroup(
  group: GroupResponse,
): group is GroupResponse & { id: number } {
  return !!group && Number.isSafeInteger(group.id) && (group.id ?? 0) > 0;
}
export function groupPermissions(
  members: GroupMemberResponse[],
  userId: number | null,
) {
  const role =
    userId === null
      ? undefined
      : members.find((member) => member.userId === userId)?.role;
  return {
    role,
    canAddMembers: role === 'OWNER' || role === 'ADMIN',
    canRename: role === 'OWNER' || role === 'ADMIN',
    canDelete: role === 'OWNER',
  };
}
export function groupError(error: unknown): string {
  return apiErrorMessage(error, groupsContent.errors);
}

export function canRemoveMember(
  group: GroupResponse,
  members: GroupMemberResponse[],
  userId: number | null,
  target: GroupMemberResponse,
): boolean {
  return canRemoveMemberWithRole(
    group,
    groupPermissions(members, userId).role,
    userId,
    target,
  );
}

export function canRemoveMemberWithRole(
  group: GroupResponse,
  role: GroupMemberResponse['role'],
  userId: number | null,
  target: GroupMemberResponse,
): boolean {
  const targetId = target.userId;
  if (
    !Number.isSafeInteger(targetId) ||
    (targetId ?? 0) <= 0 ||
    userId === null ||
    targetId === userId ||
    targetId === group.creatorId ||
    target.role === 'OWNER'
  )
    return false;
  return (
    (role === 'OWNER' &&
      (target.role === 'ADMIN' || target.role === 'MEMBER')) ||
    (role === 'ADMIN' && target.role === 'MEMBER')
  );
}
