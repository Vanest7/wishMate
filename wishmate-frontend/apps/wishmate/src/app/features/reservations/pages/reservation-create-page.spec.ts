import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import {
  ActivatedRoute,
  convertToParamMap,
  provideRouter,
} from '@angular/router';
import {
  GiftReservationResponse,
  GiftReservationsService,
} from '@wishmate/api-client';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { SessionService } from '../../../core/auth/session.service';
import { ReservationsDataService } from '../data-access/reservations-data.service';
import { ReservationTarget } from '../reservation-utils';
import { ReservationCreatePage } from './reservation-create-page';

const target: ReservationTarget = {
  group: { id: 1, name: 'Familia' },
  event: { id: 2, groupId: 1, name: 'Navidad', status: 'ACTIVE' },
  wish: { id: 5, eventId: 2, userId: 8, name: 'Libro' },
};
describe('Reservation confirmation', () => {
  let response: Subject<GiftReservationResponse>;
  let params: BehaviorSubject<ReturnType<typeof convertToParamMap>>;
  const getTarget = vi.fn();
  const createReservation = vi.fn();
  beforeEach(() => {
    response = new Subject();
    params = new BehaviorSubject(
      convertToParamMap({ groupId: '1', eventId: '2', wishId: '5' }),
    );
    getTarget.mockReset().mockReturnValue(of(target));
    createReservation.mockReset().mockReturnValue(response);
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { paramMap: params } },
        { provide: GiftReservationsService, useValue: { createReservation } },
      ],
    }).overrideComponent(ReservationCreatePage, {
      set: {
        providers: [
          { provide: ReservationsDataService, useValue: { getTarget } },
        ],
      },
    });
    TestBed.inject(SessionService).start(
      `a.${btoa(JSON.stringify({ sub: '7' }))}.c`,
      900,
    );
  });
  it('waits for confirmation and blocks repeated writes while pending and after success', async () => {
    const fixture = TestBed.createComponent(ReservationCreatePage);
    await fixture.whenStable();
    expect(createReservation).not.toHaveBeenCalled();
    expect(fixture.nativeElement.textContent).toContain('Familia');
    expect(fixture.nativeElement.textContent).toContain('Navidad');
    fixture.componentInstance.reserve();
    fixture.componentInstance.reserve();
    expect(createReservation).toHaveBeenCalledExactlyOnceWith({
      groupId: 1,
      eventId: 2,
      wishId: 5,
    });
    response.next({ id: 10, wishId: 5 });
    response.complete();
    fixture.componentInstance.reserve();
    expect(fixture.componentInstance.completed()).toBe(true);
    expect(createReservation).toHaveBeenCalledTimes(1);
  });
  it.each(['DRAFT', 'CLOSED', 'ARCHIVED'] as const)(
    'does not reserve in %s events',
    async (status) => {
      getTarget.mockReturnValue(
        of({ ...target, event: { ...target.event, status } }),
      );
      const fixture = TestBed.createComponent(ReservationCreatePage);
      await fixture.whenStable();
      fixture.componentInstance.reserve();
      expect(createReservation).not.toHaveBeenCalled();
      expect(fixture.nativeElement.textContent).toContain(
        'Solo puedes reservar regalos en eventos activos',
      );
    },
  );
  it('rejects an own wish without making reservation calls', async () => {
    getTarget.mockReturnValue(
      of({ ...target, wish: { ...target.wish, userId: 7 } }),
    );
    const fixture = TestBed.createComponent(ReservationCreatePage);
    await fixture.whenStable();
    fixture.componentInstance.reserve();
    expect(fixture.componentInstance.target()).toBeNull();
    expect(createReservation).not.toHaveBeenCalled();
  });
  it('reports conflicts without claiming success or availability', async () => {
    const fixture = TestBed.createComponent(ReservationCreatePage);
    await fixture.whenStable();
    fixture.componentInstance.reserve();
    response.error(new HttpErrorResponse({ status: 409 }));
    expect(fixture.componentInstance.completed()).toBe(false);
    expect(fixture.componentInstance.pending()).toBe(false);
    expect(fixture.componentInstance.error()).toContain(
      'puede estar reservado',
    );
  });
  it('ignores a late write response after navigating to another wish', async () => {
    const fixture = TestBed.createComponent(ReservationCreatePage);
    await fixture.whenStable();
    fixture.componentInstance.reserve();
    getTarget.mockReturnValue(
      of({ ...target, wish: { ...target.wish, id: 6 } }),
    );
    params.next(convertToParamMap({ groupId: '1', eventId: '2', wishId: '6' }));
    response.next({ id: 10, wishId: 5 });
    response.complete();
    expect(fixture.componentInstance.completed()).toBe(false);
    expect(fixture.componentInstance.target()?.wish.id).toBe(6);
  });
  it('clears the target and ignores late results after logout', async () => {
    const fixture = TestBed.createComponent(ReservationCreatePage);
    await fixture.whenStable();
    fixture.componentInstance.reserve();
    TestBed.inject(SessionService).clear();
    await fixture.whenStable();
    response.next({ id: 10, wishId: 5 });
    response.complete();
    expect(fixture.componentInstance.completed()).toBe(false);
    expect(fixture.componentInstance.target()).toBeNull();
  });
  it('does not accept a response for a different wish', async () => {
    const fixture = TestBed.createComponent(ReservationCreatePage);
    await fixture.whenStable();
    fixture.componentInstance.reserve();
    response.next({ id: 10, wishId: 8 });
    response.complete();
    expect(fixture.componentInstance.completed()).toBe(false);
    expect(fixture.componentInstance.error()).toContain(
      'Consulta Mis reservas',
    );
  });
});
