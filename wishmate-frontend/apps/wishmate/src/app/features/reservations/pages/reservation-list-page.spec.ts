import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { GiftReservationsService } from '@wishmate/api-client';
import { of, Subject, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { SessionService } from '../../../core/auth/session.service';
import { ReservationsDataService } from '../data-access/reservations-data.service';
import { ReservationCard } from '../reservation-utils';
import { ReservationListPage } from './reservation-list-page';

const card: ReservationCard = {
  reservation: { id: 9, wishId: 5 },
  date: null,
  target: {
    group: { id: 1, name: 'Familia' },
    event: { id: 2, groupId: 1, name: 'Navidad', status: 'ACTIVE' },
    wish: { id: 5, eventId: 2, userId: 8, name: 'Libro' },
  },
};
describe('My reservations', () => {
  const getMine = vi.fn();
  const cancelReservation = vi.fn();
  let confirmed: Subject<boolean>;
  let mutation: Subject<void>;
  let open: ReturnType<typeof vi.spyOn>;
  beforeEach(() => {
    confirmed = new Subject();
    mutation = new Subject();
    getMine.mockReset().mockReturnValue(of([card]));
    cancelReservation.mockReset().mockReturnValue(mutation);
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: GiftReservationsService, useValue: { cancelReservation } },
      ],
    }).overrideComponent(ReservationListPage, {
      set: {
        providers: [
          { provide: ReservationsDataService, useValue: { getMine } },
        ],
      },
    });
    TestBed.inject(SessionService).start(
      `a.${btoa(JSON.stringify({ sub: '7' }))}.c`,
      900,
    );
    open = vi.spyOn(TestBed.inject(MatDialog), 'open').mockReturnValue({
      afterClosed: () => confirmed,
      close: () => {
        confirmed.next(false);
        confirmed.complete();
      },
    } as never);
  });
  it('renders names without resource IDs or a fabricated date', async () => {
    const fixture = TestBed.createComponent(ReservationListPage);
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Familia');
    expect(fixture.nativeElement.textContent).toContain('Navidad');
    expect(fixture.nativeElement.textContent).toContain('Libro');
    expect(fixture.nativeElement.textContent).not.toContain('Grupo 1');
    expect(fixture.nativeElement.textContent).not.toContain('Reservado el');
  });
  it('requires confirmation and removes a row only after a successful empty response', async () => {
    const fixture = TestBed.createComponent(ReservationListPage);
    await fixture.whenStable();
    const page = fixture.componentInstance;
    page.cancel(card);
    page.cancel(card);
    expect(open).toHaveBeenCalledTimes(1);
    expect(cancelReservation).not.toHaveBeenCalled();
    confirmed.next(true);
    confirmed.complete();
    expect(cancelReservation).toHaveBeenCalledExactlyOnceWith({
      groupId: 1,
      eventId: 2,
      wishId: 5,
    });
    expect(page.cards()).toHaveLength(1);
    expect(page.cancelling()).toBe(true);
    mutation.next();
    mutation.complete();
    expect(page.cards()).toHaveLength(0);
    expect(page.busy()).toBe(false);
    expect(page.cancelling()).toBe(false);
  });
  it('makes no mutation if confirmation is dismissed', async () => {
    const fixture = TestBed.createComponent(ReservationListPage);
    await fixture.whenStable();
    fixture.componentInstance.cancel(card);
    confirmed.next(false);
    confirmed.complete();
    expect(cancelReservation).not.toHaveBeenCalled();
    expect(fixture.componentInstance.busy()).toBe(false);
  });
  it('keeps the reservation after a failed cancellation', async () => {
    const fixture = TestBed.createComponent(ReservationListPage);
    await fixture.whenStable();
    fixture.componentInstance.cancel(card);
    confirmed.next(true);
    confirmed.complete();
    mutation.error(new HttpErrorResponse({ status: 403 }));
    expect(fixture.componentInstance.cards()).toHaveLength(1);
    expect(fixture.componentInstance.actionError()).not.toBe('');
    expect(fixture.componentInstance.busy()).toBe(false);
  });
  it('keeps inaccessible reservations visible with no cancellation action', async () => {
    const inaccessible = { ...card, target: null };
    getMine.mockReturnValue(of([inaccessible]));
    const fixture = TestBed.createComponent(ReservationListPage);
    await fixture.whenStable();
    fixture.componentInstance.cancel(inaccessible);
    expect(open).not.toHaveBeenCalled();
    expect(fixture.nativeElement.textContent).toContain(
      'La reserva no se ha cancelado',
    );
  });
  it('does not cancel reservations in closed events', async () => {
    const target = card.target;
    if (!target) throw new Error('Missing test target');
    const closed: ReservationCard = {
      ...card,
      target: { ...target, event: { ...target.event, status: 'CLOSED' } },
    };
    getMine.mockReturnValue(of([closed]));
    const fixture = TestBed.createComponent(ReservationListPage);
    await fixture.whenStable();
    fixture.componentInstance.cancel(closed);
    expect(open).not.toHaveBeenCalled();
  });
  it('clears session data and ignores late cancellation results', async () => {
    const fixture = TestBed.createComponent(ReservationListPage);
    await fixture.whenStable();
    fixture.componentInstance.cancel(card);
    confirmed.next(true);
    confirmed.complete();
    TestBed.inject(SessionService).clear();
    await fixture.whenStable();
    mutation.next();
    mutation.complete();
    expect(fixture.componentInstance.cards()).toEqual([]);
    expect(fixture.componentInstance.success()).toBe('');
  });
  it('distinguishes an error from an empty list and allows retry', async () => {
    getMine
      .mockReturnValueOnce(throwError(() => new Error('Unavailable')))
      .mockReturnValueOnce(of([]));
    const fixture = TestBed.createComponent(ReservationListPage);
    await fixture.whenStable();
    expect(fixture.componentInstance.error()).not.toBe('');
    expect(fixture.nativeElement.textContent).not.toContain(
      'Todavía no tienes reservas',
    );
    fixture.componentInstance.retry();
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain(
      'Todavía no tienes reservas',
    );
  });
  it('wires the card action and announces a pending cancellation only once', async () => {
    const fixture = TestBed.createComponent(ReservationListPage);
    await fixture.whenStable();
    const button = (
      fixture.nativeElement as HTMLElement
    ).querySelector<HTMLButtonElement>(
      'button[aria-label="Cancelar reserva: Libro"]',
    );
    expect(button).not.toBeNull();
    button?.click();
    expect(open).toHaveBeenCalledOnce();
    confirmed.next(true);
    confirmed.complete();
    await fixture.whenStable();
    const announcements = Array.from(
      (fixture.nativeElement as HTMLElement).querySelectorAll(
        '[role="status"]',
      ),
    ).filter((element) =>
      element.textContent?.includes('Cancelando tu reserva'),
    );
    expect(announcements).toHaveLength(1);
    expect(button?.disabled).toBe(true);
  });
});
