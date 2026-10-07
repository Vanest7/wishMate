import { TestBed } from '@angular/core/testing';
import {
  EventsService,
  GroupsService,
  WishesService,
  GiftReservationsService,
} from '@wishmate/api-client';
import { firstValueFrom, of, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ReservationsDataService } from './reservations-data.service';

describe('Reservations data', () => {
  const getMyReservations = vi.fn();
  const getMyGroups = vi.fn();
  const getEvents = vi.fn();
  const getWishesByEvent = vi.fn();
  beforeEach(() => {
    getMyReservations.mockReset().mockReturnValue(of([{ id: 9, wishId: 5 }]));
    getMyGroups.mockReset().mockReturnValue(of([{ id: 1, name: 'Familia' }]));
    getEvents
      .mockReset()
      .mockReturnValue(
        of([{ id: 2, groupId: 1, name: 'Navidad', status: 'ACTIVE' }]),
      );
    getWishesByEvent
      .mockReset()
      .mockReturnValue(of([{ id: 5, eventId: 2, userId: 8, name: 'Libro' }]));
    TestBed.configureTestingModule({
      providers: [
        ReservationsDataService,
        { provide: GiftReservationsService, useValue: { getMyReservations } },
        { provide: GroupsService, useValue: { getMyGroups } },
        { provide: EventsService, useValue: { getEvents } },
        { provide: WishesService, useValue: { getWishesByEvent } },
      ],
    });
  });
  it('resolves names and parent IDs without inventing a date', async () => {
    const cards = await firstValueFrom(
      TestBed.inject(ReservationsDataService).getMine(7),
    );
    expect(cards[0].target?.group.name).toBe('Familia');
    expect(cards[0].target?.event.name).toBe('Navidad');
    expect(cards[0].target?.wish.name).toBe('Libro');
    expect(cards[0].date).toBeNull();
  });
  it('makes no catalog calls when there are no reservations', async () => {
    getMyReservations.mockReturnValue(of([]));
    expect(
      await firstValueFrom(TestBed.inject(ReservationsDataService).getMine(7)),
    ).toEqual([]);
    expect(getMyGroups).not.toHaveBeenCalled();
  });
  it('preserves inaccessible reservations instead of turning them into an empty success', async () => {
    getEvents.mockReturnValue(throwError(() => new Error('Access denied')));
    const cards = await firstValueFrom(
      TestBed.inject(ReservationsDataService).getMine(7),
    );
    expect(cards).toHaveLength(1);
    expect(cards[0].target).toBeNull();
  });
  it('keeps rows unresolved when group lookup fails', async () => {
    getMyGroups.mockReturnValue(throwError(() => new Error('Offline')));
    const cards = await firstValueFrom(
      TestBed.inject(ReservationsDataService).getMine(7),
    );
    expect(cards[0].reservation.id).toBe(9);
    expect(cards[0].target).toBeNull();
  });
  it('does not attach reservation data to an own wish', async () => {
    getWishesByEvent.mockReturnValue(
      of([{ id: 5, eventId: 2, userId: 7, name: 'Own wish' }]),
    );
    const cards = await firstValueFrom(
      TestBed.inject(ReservationsDataService).getMine(7),
    );
    expect(cards[0].target).toBeNull();
  });
  it('rejects malformed reservation identifiers', async () => {
    getMyReservations.mockReturnValue(of([{ id: 9 }]));
    await expect(
      firstValueFrom(TestBed.inject(ReservationsDataService).getMine(7)),
    ).rejects.toThrow('Invalid reservations');
  });
  it('propagates reservation list failures', async () => {
    getMyReservations.mockReturnValue(
      throwError(() => new Error('Unavailable')),
    );
    await expect(
      firstValueFrom(TestBed.inject(ReservationsDataService).getMine(7)),
    ).rejects.toThrow('Unavailable');
  });
});
