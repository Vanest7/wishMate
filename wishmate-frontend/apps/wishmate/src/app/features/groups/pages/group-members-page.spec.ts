import { HttpErrorResponse } from '@angular/common/http';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ROUTER_OUTLET_DATA } from '@angular/router';
import { OverlayContainer } from '@angular/cdk/overlay';
import { GroupMembersService, GroupMemberResponse } from '@wishmate/api-client';
import { Subject } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { SessionService } from '../../../core/auth/session.service';
import { canRemoveMember } from '../group-utils';
import { GroupMembersPage } from './group-members-page';

describe('Member removal', () => {
  it.each([
    ['OWNER', 'MEMBER', true],
    ['OWNER', 'ADMIN', true],
    ['ADMIN', 'MEMBER', true],
    ['ADMIN', 'ADMIN', false],
    ['MEMBER', 'MEMBER', false],
    ['OWNER', 'OWNER', false],
  ] as const)('allows %s to remove %s: %s', (actor, target, expected) => {
    expect(
      canRemoveMember({ creatorId: 1 }, [{ userId: 7, role: actor }], 7, {
        userId: 8,
        role: target,
      }),
    ).toBe(expected);
  });
  it('protects self, creator, unknown identity and invalid target IDs', () => {
    const members: GroupMemberResponse[] = [{ userId: 7, role: 'OWNER' }];
    for (const userId of [7, 1, undefined, 0]) {
      expect(
        canRemoveMember({ creatorId: 1 }, members, 7, {
          userId,
          role: 'MEMBER',
        }),
      ).toBe(false);
    }
    expect(
      canRemoveMember({}, members, null, { userId: 8, role: 'MEMBER' }),
    ).toBe(false);
  });
  let response: Subject<void>;
  let removeMember: ReturnType<typeof vi.fn>;
  let memberRemoved: ReturnType<typeof vi.fn>;
  beforeEach(() => {
    response = new Subject<void>();
    removeMember = vi.fn(() => response);
    memberRemoved = vi.fn();
    TestBed.configureTestingModule({
      providers: [
        { provide: GroupMembersService, useValue: { removeMember } },
        {
          provide: ROUTER_OUTLET_DATA,
          useValue: signal({
            group: { id: 3, creatorId: 7 },
            members: [
              { userId: 7, role: 'OWNER' },
              { userId: 8, role: 'MEMBER', userName: 'Alex' },
            ],
            memberRemoved,
          }),
        },
      ],
    });
    TestBed.inject(SessionService).start(
      `a.${btoa(JSON.stringify({ sub: '7' }))}.c`,
      3600,
    );
  });
  async function setup() {
    const fixture = TestBed.createComponent(GroupMembersPage);
    await fixture.whenStable();
    const buttons = (
      fixture.nativeElement as HTMLElement
    ).querySelectorAll<HTMLButtonElement>('.remove-member');
    expect(buttons.length).toBe(1);
    buttons[0].click();
    await fixture.whenStable();
    expect(
      TestBed.inject(OverlayContainer).getContainerElement().textContent,
    ).toContain('Alex');
    expect(removeMember).not.toHaveBeenCalled();
    return fixture;
  }
  it('confirms once and updates the shared list only after a successful 204 response', async () => {
    const fixture = await setup();
    const page = fixture.componentInstance;
    page.confirmRemove();
    page.confirmRemove();
    page.cancelRemove();
    expect(removeMember).toHaveBeenCalledExactlyOnceWith({
      groupId: 3,
      userId: 8,
    });
    expect(memberRemoved).not.toHaveBeenCalled();
    response.next();
    response.complete();
    expect(memberRemoved).toHaveBeenCalledExactlyOnceWith(3, 8);
    expect(page.pending()).toBe(false);
  });
  it('cancels without sending a request', async () => {
    const fixture = await setup();
    fixture.componentInstance.cancelRemove();
    expect(removeMember).not.toHaveBeenCalled();
  });
  it.each([0, 403, 404, 409, 500])(
    'preserves the member after status %s',
    async (status) => {
      const fixture = await setup();
      fixture.componentInstance.confirmRemove();
      response.error(new HttpErrorResponse({ status }));
      expect(memberRemoved).not.toHaveBeenCalled();
      expect(fixture.componentInstance.error()).not.toBe('');
      expect(fixture.componentInstance.pending()).toBe(false);
      expect(fixture.componentInstance.selectedMember()?.userId).toBe(8);
    },
  );
});
