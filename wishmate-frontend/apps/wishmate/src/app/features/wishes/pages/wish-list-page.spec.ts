import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import {
  ActivatedRoute,
  convertToParamMap,
  provideRouter,
} from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import {
  EventsService,
  GroupMembersService,
  GroupsService,
  WishResponse,
  WishesService,
} from '@wishmate/api-client';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { SessionService } from '../../../core/auth/session.service';
import { WishListPage } from './wish-list-page';
import { WishDialogResult } from '../ui/wish-dialog';
import { ValidWish } from '../wish-utils';

const own: ValidWish = {
  id: 5,
  eventId: 4,
  userId: 7,
  name: 'Book',
  description: 'Hardcover',
  estimatedPrice: 25.5,
};
const other: ValidWish = {
  ...own,
  id: 6,
  userId: 8,
  name: 'Headphones',
  purchaseUrl: 'javascript:alert(1)',
};

describe('Wish list', () => {
  let wishes: Subject<WishResponse[]>;
  let params: BehaviorSubject<ReturnType<typeof convertToParamMap>>;
  let api: {
    getWishesByEvent: ReturnType<typeof vi.fn>;
    getWishReservationStatus: ReturnType<typeof vi.fn>;
  };
  let events: { getEvent: ReturnType<typeof vi.fn> };
  beforeEach(() => {
    wishes = new Subject();
    params = new BehaviorSubject(
      convertToParamMap({ groupId: '1', eventId: '4' }),
    );
    api = {
      getWishesByEvent: vi.fn(() => wishes),
      getWishReservationStatus: vi.fn(() => of('AVAILABLE')),
    };
    events = {
      getEvent: vi.fn(() =>
        of({ id: 4, groupId: 1, name: 'Cumpleaños de Ana', status: 'ACTIVE' }),
      ),
    };
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: WishesService, useValue: api },
        {
          provide: GroupsService,
          useValue: { getGroup: () => of({ id: 1, name: 'Familia García' }) },
        },
        { provide: EventsService, useValue: events },
        {
          provide: GroupMembersService,
          useValue: {
            getMembers: () =>
              of([
                { userId: 7, role: 'OWNER', userName: 'Ana' },
                { userId: 8, role: 'MEMBER', userName: 'Alex' },
              ]),
          },
        },
        { provide: ActivatedRoute, useValue: { paramMap: params } },
      ],
    });
    TestBed.inject(SessionService).start(
      `a.${btoa(JSON.stringify({ sub: '7' }))}.c`,
      3600,
    );
  });
  it('shows group/event names, author names and only own actions without reservation details', async () => {
    const fixture = TestBed.createComponent(WishListPage);
    await fixture.whenStable();
    wishes.next([
      { ...own, reserved: true, reserverName: 'Secret Santa' } as WishResponse,
      other,
    ]);
    wishes.complete();
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;
    expect(element.textContent).toContain('Familia García');
    expect(element.textContent).toContain('Cumpleaños de Ana');
    expect(element.textContent).toContain('Alex');
    expect(element.textContent).not.toContain('Grupo 1');
    expect(element.textContent).not.toContain('Evento 4');
    expect(element.textContent).not.toContain('Secret Santa');
    expect(element.textContent?.toLowerCase()).not.toContain('reservad');
    expect(element.querySelectorAll('.delete-action').length).toBe(1);
    expect(
      element.querySelector('a[href="/reservations/new/1/4/6"]'),
    ).not.toBeNull();
    expect(
      element.querySelector('a[href="/reservations/new/1/4/5"]'),
    ).toBeNull();
    expect(element.querySelector('a[href^="javascript:"]')).toBeNull();
    expect(element.textContent).toContain('25,50');
    fixture.componentInstance.mineOnly.set(true);
    await fixture.whenStable();
    expect(element.textContent).toContain('Book');
    expect(element.textContent).not.toContain('Headphones');
    expect(element.querySelector('a[href^="/reservations/new"]')).toBeNull();
  });
  it.each(['DRAFT', 'CLOSED', 'ARCHIVED'] as const)(
    'hides all write actions when %s',
    async (status) => {
      events.getEvent.mockReturnValue(
        of({ id: 4, groupId: 1, name: 'Birthday', status }),
      );
      const fixture = TestBed.createComponent(WishListPage);
      await fixture.whenStable();
      wishes.next([own]);
      wishes.complete();
      await fixture.whenStable();
      expect(fixture.componentInstance.canCreate()).toBe(false);
      expect(
        (fixture.nativeElement as HTMLElement).querySelector('.delete-action'),
      ).toBeNull();
    },
  );
  it('distinguishes loading, errors and empty states and retries', async () => {
    const fixture = TestBed.createComponent(WishListPage);
    await fixture.whenStable();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'Cargando deseos',
    );
    wishes.error(new HttpErrorResponse({ status: 403 }));
    await fixture.whenStable();
    expect(fixture.componentInstance.error()).not.toBe('');
    expect(fixture.componentInstance.data()).toBeNull();
    wishes = new Subject();
    fixture.componentInstance.retry();
    wishes.next([]);
    wishes.complete();
    await fixture.whenStable();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'Todavía no hay deseos',
    );
  });
  it('rejects a wish from another event', async () => {
    const fixture = TestBed.createComponent(WishListPage);
    await fixture.whenStable();
    wishes.next([{ ...own, eventId: 8 }]);
    wishes.complete();
    expect(fixture.componentInstance.data()).toBeNull();
    expect(fixture.componentInstance.error()).not.toBe('');
  });
  it('cancels old requests on navigation', async () => {
    const fixture = TestBed.createComponent(WishListPage);
    await fixture.whenStable();
    const old = wishes;
    wishes = new Subject();
    params.next(convertToParamMap({ groupId: '1', eventId: '9' }));
    expect(old.observed).toBe(false);
    expect(fixture.componentInstance.data()).toBeNull();
    expect(api.getWishesByEvent).toHaveBeenLastCalledWith({
      groupId: 1,
      eventId: 9,
    });
  });
  it('clears private data when the session ends', async () => {
    const fixture = TestBed.createComponent(WishListPage);
    await fixture.whenStable();
    wishes.next([own]);
    wishes.complete();
    TestBed.inject(SessionService).clear();
    await fixture.whenStable();
    expect(fixture.componentInstance.data()).toBeNull();
    expect(fixture.componentInstance.cards()).toEqual([]);
  });
  it('guards foreign edits and duplicate dialogs, then removes the item after confirmation', async () => {
    const closed = new Subject<WishDialogResult>();
    const open = vi
      .spyOn(TestBed.inject(MatDialog), 'open')
      .mockReturnValue({ afterClosed: () => closed, close: vi.fn() } as never);
    const fixture = TestBed.createComponent(WishListPage);
    await fixture.whenStable();
    wishes.next([own, other]);
    wishes.complete();
    const page = fixture.componentInstance;
    page.edit('delete', other);
    expect(open).not.toHaveBeenCalled();
    page.edit('delete', own);
    page.edit('delete', own);
    expect(open).toHaveBeenCalledTimes(1);
    expect(page.data()?.wishes.length).toBe(2);
    closed.next({ mode: 'delete', wishId: own.id });
    closed.complete();
    expect(page.data()?.wishes.map((wish) => wish.id)).toEqual([other.id]);
    expect(api.getWishesByEvent).toHaveBeenCalledTimes(1);
  });
  it('switches views without reloading and preserves the selected filter', async () => {
    const fixture = TestBed.createComponent(WishListPage);
    await fixture.whenStable();
    wishes.next([own, other]);
    wishes.complete();
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelectorAll('app-wish-card')).toHaveLength(2);
    const statusRequestCount = api.getWishReservationStatus.mock.calls.length;
    const buttons = element.querySelectorAll<HTMLButtonElement>(
      '.view-switch button',
    );
    buttons[1].click();
    await fixture.whenStable();
    expect(buttons[1].getAttribute('aria-pressed')).toBe('true');
    expect(element.querySelector('app-wish-card')).toBeNull();
    expect(element.querySelectorAll('tbody tr')).toHaveLength(2);
    element.querySelectorAll<HTMLButtonElement>('.filters button')[1].click();
    await fixture.whenStable();
    expect(element.querySelectorAll('tbody tr')).toHaveLength(1);
    expect(element.querySelector('tbody')?.textContent).toContain('Book');
    expect(element.querySelector('tbody')?.textContent).not.toContain(
      'Headphones',
    );
    buttons[0].click();
    await fixture.whenStable();
    expect(element.querySelector('table')).toBeNull();
    expect(element.querySelectorAll('app-wish-card')).toHaveLength(1);
    expect(api.getWishesByEvent).toHaveBeenCalledTimes(1);
    expect(api.getWishReservationStatus).toHaveBeenCalledTimes(
      statusRequestCount,
    );
  });
  it('routes table delete actions through the existing confirmation dialog', async () => {
    const closed = new Subject<WishDialogResult>();
    const open = vi.spyOn(TestBed.inject(MatDialog), 'open').mockReturnValue({
      afterClosed: () => closed,
      close: vi.fn(),
    } as never);
    const fixture = TestBed.createComponent(WishListPage);
    await fixture.whenStable();
    wishes.next([own, other]);
    wishes.complete();
    fixture.componentInstance.viewMode.set('table');
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;
    element.querySelector<HTMLButtonElement>('.delete-action')?.click();
    expect(open).toHaveBeenCalledOnce();
    expect(fixture.componentInstance.data()?.wishes).toHaveLength(2);
    closed.next({ mode: 'delete', wishId: own.id });
    closed.complete();
    await fixture.whenStable();
    expect(element.querySelectorAll('tbody tr')).toHaveLength(1);
    expect(element.querySelector('tbody')?.textContent).toContain('Headphones');
  });
});
