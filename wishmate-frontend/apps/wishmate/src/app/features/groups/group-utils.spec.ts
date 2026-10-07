import { describe, expect, it } from 'vitest';
import { groupPermissions } from './group-utils';
describe('Group permissions', () => {
  it.each([
    ['OWNER', true, true],
    ['ADMIN', true, false],
    ['MEMBER', false, false],
    [undefined, false, false],
  ] as const)(
    'derives actions for %s from the current membership',
    (role, canRename, canDelete) => {
      expect(groupPermissions([{ userId: 7, role }], 7)).toEqual({
        role,
        canRename,
        canDelete,
      });
    },
  );
  it('never inherits another member permission or grants an unknown identity access', () => {
    const members = [{ userId: 8, role: 'OWNER' as const }];
    expect(groupPermissions(members, 7).canDelete).toBe(false);
    expect(groupPermissions(members, null).canRename).toBe(false);
  });
});
