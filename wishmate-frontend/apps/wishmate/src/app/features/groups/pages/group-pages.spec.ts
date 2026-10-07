import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import {
  ActivatedRoute,
  convertToParamMap,
  provideRouter,
} from '@angular/router';
import { OverlayContainer } from '@angular/cdk/overlay';
import { MatDialog } from '@angular/material/dialog';
import {
  GroupsService,
  GroupMembersService,
  GroupResponse,
  GroupMemberResponse,
} from '@wishmate/api-client';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { SessionService } from '../../../core/auth/session.service';
import { groupsContent } from '../groups.content';
import { GroupListPage } from './group-list-page';
import { GroupDetailPage } from './group-detail-page';

describe('Group pages', () => {
  let list: Subject<GroupResponse[]>;
  let details: Subject<GroupResponse>;
  let members: Subject<GroupMemberResponse[]>;
  let params: BehaviorSubject<ReturnType<typeof convertToParamMap>>;
  let api: {
    getMyGroups: ReturnType<typeof vi.fn>;
    getGroup: ReturnType<typeof vi.fn>;
  };
  beforeEach(() => {
    list = new Subject();
    details = new Subject();
    members = new Subject();
    params = new BehaviorSubject(convertToParamMap({ groupId: '1' }));
    api = { getMyGroups: vi.fn(() => list), getGroup: vi.fn(() => details) };
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: GroupsService, useValue: api },
        {
          provide: GroupMembersService,
          useValue: { getMembers: vi.fn(() => members) },
        },
        {
          provide: ActivatedRoute,
          useValue: { paramMap: params, data: of({}), snapshot: {} },
        },
      ],
    });
  });
  it('distinguishes loading, empty and error states and supports retry', async () => {
    const fixture = TestBed.createComponent(GroupListPage);
    await fixture.whenStable();
    const text = () => (fixture.nativeElement as HTMLElement).textContent;
    expect(text()).toContain('Cargando tus grupos');
    list.next([]);
    list.complete();
    await fixture.whenStable();
    expect(text()).toContain(groupsContent.emptyTitle);
    list = new Subject();
    fixture.componentInstance.retry();
    list.error(new HttpErrorResponse({ status: 500 }));
    await fixture.whenStable();
    expect(text()).toContain('No hemos podido');
    expect(text()).not.toContain('Todo empieza');
    list = new Subject();
    fixture.componentInstance.retry();
    list.next([{ id: 7, name: 'Familia' }]);
    list.complete();
    await fixture.whenStable();
    expect(text()).toContain('Familia');
  });
  it('does not render invalid group links', () => {
    const page = TestBed.createComponent(GroupListPage).componentInstance;
    list.next([{ name: 'Invalid' }]);
    list.complete();
    expect(page.error()).not.toBe('');
    expect(page.groups()).toEqual([]);
  });
  it('refreshes the list only after a successful create', () => {
    let closed = new Subject<unknown>();
    vi.spyOn(TestBed.inject(MatDialog), 'open').mockReturnValue({
      afterClosed: () => closed,
      close: vi.fn(),
    } as never);
    const page = TestBed.createComponent(GroupListPage).componentInstance;
    list.next([]);
    list.complete();
    page.create();
    page.create();
    expect(TestBed.inject(MatDialog).open).toHaveBeenCalledTimes(1);
    closed.next(undefined);
    closed.complete();
    expect(api.getMyGroups).toHaveBeenCalledTimes(1);
    closed = new Subject<unknown>();
    page.create();
    closed.next({ mode: 'create', group: { id: 4 } });
    closed.complete();
    expect(api.getMyGroups).toHaveBeenCalledTimes(2);
  });
  it.each(['OWNER', 'ADMIN', 'MEMBER'] as const)(
    'shows only allowed actions for %s',
    async (role) => {
      TestBed.inject(SessionService).start(
        `a.${btoa(JSON.stringify({ sub: '7' }))}.c`,
        3600,
      );
      const fixture = TestBed.createComponent(GroupDetailPage);
      details.next({ id: 1, name: 'Familia' });
      details.complete();
      members.next([{ userId: 7, role }]);
      members.complete();
      await fixture.whenStable();
      const element = fixture.nativeElement as HTMLElement;
      expect(element.textContent?.includes('Añadir miembros')).toBe(
        role !== 'MEMBER',
      );
      const trigger =
        element.querySelector<HTMLButtonElement>('.settings-trigger');
      expect(!!trigger).toBe(role !== 'MEMBER');
      if (trigger) {
        trigger.click();
        await fixture.whenStable();
        const menu = TestBed.inject(OverlayContainer).getContainerElement();
        expect(menu.textContent).toContain('Cambiar nombre');
        expect(menu.textContent?.includes('Eliminar grupo')).toBe(
          role === 'OWNER',
        );
      }
    },
  );
  it('opens settings on hover', async () => {
    TestBed.inject(SessionService).start(
      `a.${btoa(JSON.stringify({ sub: '7' }))}.c`,
      3600,
    );
    const fixture = TestBed.createComponent(GroupDetailPage);
    details.next({ id: 1, name: 'Familia' });
    details.complete();
    members.next([{ userId: 7, role: 'OWNER' }]);
    members.complete();
    await fixture.whenStable();
    const trigger = (fixture.nativeElement as HTMLElement).querySelector(
      '.settings-trigger',
    );
    trigger?.dispatchEvent(new MouseEvent('mouseenter'));
    await fixture.whenStable();
    expect(
      TestBed.inject(OverlayContainer).getContainerElement().textContent,
    ).toContain('Cambiar nombre');
    fixture.destroy();
  });
  it('guards member additions and refreshes only after success', () => {
    const closed = new Subject<boolean | undefined>();
    vi.spyOn(TestBed.inject(MatDialog), 'open').mockReturnValue({
      afterClosed: () => closed,
      close: vi.fn(),
    } as never);
    const page = TestBed.createComponent(GroupDetailPage).componentInstance;
    page.addMember();
    expect(TestBed.inject(MatDialog).open).not.toHaveBeenCalled();
    TestBed.inject(SessionService).start(
      `a.${btoa(JSON.stringify({ sub: '7' }))}.c`,
      3600,
    );
    details.next({ id: 1, name: 'Familia' });
    details.complete();
    members.next([{ userId: 7, role: 'ADMIN' }]);
    members.complete();
    page.addMember();
    page.addMember();
    expect(TestBed.inject(MatDialog).open).toHaveBeenCalledTimes(1);
    expect(api.getGroup).toHaveBeenCalledTimes(1);
    closed.next(true);
    closed.complete();
    expect(api.getGroup).toHaveBeenCalledTimes(2);
    expect(page.success()).toBe('Miembro añadido.');
  });
  it('clears previous data and cancels stale reads on group changes', () => {
    const page = TestBed.createComponent(GroupDetailPage).componentInstance;
    const oldDetails = details;
    const oldMembers = members;
    details = new Subject();
    members = new Subject();
    params.next(convertToParamMap({ groupId: '2' }));
    expect(oldDetails.observed).toBe(false);
    expect(oldMembers.observed).toBe(false);
    expect(page.group()).toBeNull();
    expect(page.permissions().canDelete).toBe(false);
    details.next({ id: 2, name: 'Amigos' });
    details.complete();
    members.next([]);
    members.complete();
    expect(page.group()?.name).toBe('Amigos');
  });
  it('shows denied access without rendering group actions', async () => {
    const fixture = TestBed.createComponent(GroupDetailPage);
    details.error(new HttpErrorResponse({ status: 403 }));
    await fixture.whenStable();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'No se ha autorizado',
    );
    expect(fixture.componentInstance.group()).toBeNull();
    expect(fixture.componentInstance.permissions().canDelete).toBe(false);
  });
});
