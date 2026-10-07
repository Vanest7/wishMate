import { HttpErrorResponse } from '@angular/common/http';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  ActivatedRoute,
  convertToParamMap,
  provideRouter,
  ROUTER_OUTLET_DATA,
} from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { EventResponse, EventsService } from '@wishmate/api-client';
import { BehaviorSubject, Subject } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { EventListPage } from './event-list-page';
import { EventDetailPage } from './event-detail-page';

const current: EventResponse = {
  id: 4,
  groupId: 1,
  name: 'Birthday',
  status: 'ACTIVE',
};

describe('Event pages', () => {
  let list: Subject<EventResponse[]>;
  let detail: Subject<EventResponse>;
  let params: BehaviorSubject<ReturnType<typeof convertToParamMap>>;
  let api: {
    getEvents: ReturnType<typeof vi.fn>;
    getEvent: ReturnType<typeof vi.fn>;
  };
  const context = signal({ group: { id: 1, name: 'Family' } });
  beforeEach(() => {
    list = new Subject();
    detail = new Subject();
    params = new BehaviorSubject(convertToParamMap({ eventId: '4' }));
    context.set({ group: { id: 1, name: 'Family' } });
    api = { getEvents: vi.fn(() => list), getEvent: vi.fn(() => detail) };
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: EventsService, useValue: api },
        { provide: ROUTER_OUTLET_DATA, useValue: context },
        { provide: ActivatedRoute, useValue: { paramMap: params } },
      ],
    });
  });
  it('shows loading, empty, error and retry states', async () => {
    const fixture = TestBed.createComponent(EventListPage);
    await fixture.whenStable();
    const text = () => (fixture.nativeElement as HTMLElement).textContent;
    expect(text()).toContain('Cargando eventos');
    list.next([]);
    list.complete();
    await fixture.whenStable();
    expect(text()).toContain('La próxima celebración');
    list = new Subject();
    fixture.componentInstance.retry();
    list.error(new HttpErrorResponse({ status: 403 }));
    await fixture.whenStable();
    expect(text()).toContain('No se ha autorizado');
    expect(text()).not.toContain('La próxima celebración');
    list = new Subject();
    fixture.componentInstance.retry();
    list.next([current]);
    list.complete();
    await fixture.whenStable();
    expect(text()).toContain('Birthday');
    expect(text()).toContain('Family');
  });
  it('cancels stale list reads when changing groups', async () => {
    const fixture = TestBed.createComponent(EventListPage);
    await fixture.whenStable();
    const old = list;
    list = new Subject();
    context.set({ group: { id: 2, name: 'Friends' } });
    await fixture.whenStable();
    expect(old.observed).toBe(false);
    expect(api.getEvents).toHaveBeenLastCalledWith({ groupId: 2 });
    expect(fixture.componentInstance.events()).toEqual([]);
  });
  it('rejects invalid or cross-group list entries', async () => {
    const fixture = TestBed.createComponent(EventListPage);
    await fixture.whenStable();
    list.next([{ ...current, groupId: 2 }]);
    expect(fixture.componentInstance.error()).not.toBe('');
    expect(fixture.componentInstance.events()).toEqual([]);
  });
  it('guards duplicate create dialogs and refreshes only after success', async () => {
    const closed = new Subject<EventResponse>();
    const open = vi
      .spyOn(TestBed.inject(MatDialog), 'open')
      .mockReturnValue({ afterClosed: () => closed, close: vi.fn() } as never);
    const fixture = TestBed.createComponent(EventListPage);
    await fixture.whenStable();
    list.next([]);
    list.complete();
    fixture.componentInstance.create();
    fixture.componentInstance.create();
    expect(open).toHaveBeenCalledTimes(1);
    expect(api.getEvents).toHaveBeenCalledTimes(1);
    closed.next(current);
    closed.complete();
    expect(api.getEvents).toHaveBeenCalledTimes(2);
  });
  it.each(['DRAFT', 'ACTIVE', 'CLOSED', 'ARCHIVED'] as const)(
    'renders permitted actions for %s',
    async (status) => {
      const fixture = TestBed.createComponent(EventDetailPage);
      await fixture.whenStable();
      detail.next({ ...current, status });
      detail.complete();
      await fixture.whenStable();
      const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
      expect(text.includes('Editar evento')).toBe(
        status === 'DRAFT' || status === 'ACTIVE',
      );
      expect(text.includes('Activar evento')).toBe(status === 'DRAFT');
      expect(text.includes('Cerrar evento')).toBe(status === 'ACTIVE');
      expect(text.includes('Archivar evento')).toBe(status === 'CLOSED');
    },
  );
  it('clears data and unsubscribes stale detail reads on navigation', async () => {
    const fixture = TestBed.createComponent(EventDetailPage);
    await fixture.whenStable();
    const old = detail;
    detail = new Subject();
    params.next(convertToParamMap({ eventId: '5' }));
    expect(old.observed).toBe(false);
    expect(fixture.componentInstance.event()).toBeNull();
    detail.next({ ...current, id: 5 });
    detail.complete();
    expect(fixture.componentInstance.event()?.id).toBe(5);
  });
  it('does not apply a mutation response to a different event after navigation', async () => {
    const closed = new Subject<EventResponse>();
    vi.spyOn(TestBed.inject(MatDialog), 'open').mockReturnValue({
      afterClosed: () => closed,
      close: vi.fn(),
    } as never);
    const fixture = TestBed.createComponent(EventDetailPage);
    await fixture.whenStable();
    detail.next(current);
    detail.complete();
    fixture.componentInstance.act('close');
    detail = new Subject();
    params.next(convertToParamMap({ eventId: '5' }));
    detail.next({ ...current, id: 5 });
    detail.complete();
    closed.next({ ...current, status: 'CLOSED' });
    closed.complete();
    expect(fixture.componentInstance.event()?.id).toBe(5);
    expect(fixture.componentInstance.event()?.status).toBe('ACTIVE');
  });
});
