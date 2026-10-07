import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import {
  GroupMemberResponse,
  GroupMembersService,
  SearchUserResponse,
  UsersService,
} from '@wishmate/api-client';
import { of, Subject } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AddMemberDialog } from './add-member-dialog';

describe('Add member dialog', () => {
  let response: Subject<GroupMemberResponse>;
  let api: { addMember: ReturnType<typeof vi.fn> };
  let usersApi: { search: ReturnType<typeof vi.fn> };
  let ref: { close: ReturnType<typeof vi.fn>; disableClose: boolean };

  beforeEach(() => {
    response = new Subject();
    api = { addMember: vi.fn(() => response) };
    usersApi = { search: vi.fn(() => of([])) };
    ref = { close: vi.fn(), disableClose: false };
    TestBed.configureTestingModule({
      providers: [
        { provide: GroupMembersService, useValue: api },
        { provide: UsersService, useValue: usersApi },
        { provide: MatDialogRef, useValue: ref },
        {
          provide: MAT_DIALOG_DATA,
          useValue: { groupId: 3, memberIds: [7] },
        },
      ],
    });
  });

  it('requires selecting a search result', () => {
    const page = TestBed.createComponent(AddMemberDialog).componentInstance;
    page.user.setValue('Alex');
    page.submit();
    expect(api.addMember).not.toHaveBeenCalled();
    expect(page.user.touched).toBe(true);
  });

  it('searches after two characters and excludes current members', async () => {
    const users: SearchUserResponse[] = [
      { id: 7, userName: 'Already here', handle: 'already' },
      { id: 42, userName: 'Alex', handle: 'alex' },
    ];
    usersApi.search.mockReturnValue(of(users));
    const page = TestBed.createComponent(AddMemberDialog).componentInstance;
    page.user.setValue('Al');
    await new Promise((resolve) => setTimeout(resolve, 350));
    expect(usersApi.search).toHaveBeenCalledWith({ query: '@Al' });
    expect(page.options()).toEqual([
      { id: 42, userName: 'Alex', handle: 'alex' },
    ]);
  });

  it('submits once and blocks dismissal until the request completes', () => {
    const page = TestBed.createComponent(AddMemberDialog).componentInstance;
    page.user.setValue({ id: 42, userName: 'Alex', handle: 'alex' });
    page.submit();
    page.submit();
    page.cancel();
    expect(api.addMember).toHaveBeenCalledExactlyOnceWith({
      groupId: 3,
      body: { userId: 42 },
    });
    expect(ref.disableClose).toBe(true);
    expect(ref.close).not.toHaveBeenCalled();
    response.next({ userId: 42, role: 'MEMBER' });
    response.complete();
    expect(ref.close).toHaveBeenCalledWith(true);
    expect(page.pending()).toBe(false);
  });

  it.each([0, 400, 403, 404, 409, 500])(
    'preserves selection and permits retry after status %s',
    (status) => {
      const page = TestBed.createComponent(AddMemberDialog).componentInstance;
      page.user.setValue({ id: 42, userName: 'Alex' });
      page.submit();
      response.error(new HttpErrorResponse({ status }));
      expect(page.user.value).toEqual({
        id: 42,
        userName: 'Alex',
        handle: 'alex',
      });
      expect(page.user.enabled).toBe(true);
      expect(page.error()).not.toBe('');
      expect(page.pending()).toBe(false);
      expect(ref.disableClose).toBe(false);
      expect(ref.close).not.toHaveBeenCalled();
    },
  );

  it('shows server field validation and clears it on input changes', () => {
    const page = TestBed.createComponent(AddMemberDialog).componentInstance;
    page.user.setValue({ id: 42, userName: 'Alex' });
    page.submit();
    response.error(
      new HttpErrorResponse({
        status: 400,
        error: { fieldErrors: { userId: 'Invalid' } },
      }),
    );
    expect(page.user.hasError('server')).toBe(true);
    page.user.setValue({ id: 43, userName: 'Ana' });
    expect(page.user.valid).toBe(true);
  });

  it('unsubscribes when destroyed', () => {
    const fixture = TestBed.createComponent(AddMemberDialog);
    fixture.componentInstance.user.setValue({ id: 42, userName: 'Alex' });
    fixture.componentInstance.submit();
    fixture.destroy();
    expect(response.observed).toBe(false);
  });
});
