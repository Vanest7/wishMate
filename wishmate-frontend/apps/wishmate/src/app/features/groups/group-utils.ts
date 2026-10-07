import { HttpErrorResponse } from '@angular/common/http';
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
    canRename: role === 'OWNER' || role === 'ADMIN',
    canDelete: role === 'OWNER',
  };
}
export function groupError(error: unknown): string {
  const messages = groupsContent.errors;
  if (!(error instanceof HttpErrorResponse)) return messages.generic;
  switch (error.status) {
    case 0:
      return messages.network;
    case 400:
      return messages.validation;
    case 403:
      return messages.forbidden;
    case 404:
      return messages.missing;
    case 409:
      return messages.conflict;
    default:
      return messages.generic;
  }
}
