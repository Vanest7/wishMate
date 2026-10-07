import { TestBed } from '@angular/core/testing';
import {
  ActivatedRoute,
  convertToParamMap,
  provideRouter,
} from '@angular/router';
import {
  EventsService,
  GroupsService,
  EventResponse,
} from '@wishmate/api-client';
import { of, Subject, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { DashboardPage } from './dashboard-page';

describe('DashboardPage', () => {
  const getMyGroups = vi.fn();
  const getEvents = vi.fn();
  beforeEach(() => {
    getMyGroups.mockReset().mockReturnValue(
      of([
        { id: 1, name: 'Familia' },
        { id: 2, name: 'Amigos' },
      ]),
    );
    getEvents
      .mockReset()
      .mockReturnValue(
        of([{ id: 7, groupId: 1, name: 'Navidad', status: 'ACTIVE' }]),
      );
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            queryParamMap: of(convertToParamMap({ section: 'wishes' })),
          },
        },
        { provide: GroupsService, useValue: { getMyGroups } },
        { provide: EventsService, useValue: { getEvents } },
      ],
    });
  });
  it('renders four reusable cards and links straight to named event wishes', async () => {
    const fixture = TestBed.createComponent(DashboardPage);
    await fixture.whenStable();
    expect(
      fixture.nativeElement.querySelectorAll('app-navigation-card').length,
    ).toBe(4);
    expect(getEvents).not.toHaveBeenCalled();
    fixture.componentInstance.selectGroup(1);
    await fixture.whenStable();
    expect(
      fixture.nativeElement.querySelector(
        'a[href="/groups/1/events/7/wishes"]',
      ),
    ).not.toBeNull();
    expect(fixture.nativeElement.textContent).toContain('Navidad');
    expect(fixture.nativeElement.textContent).toContain('Familia');
  });
  it('cancels stale event reads when switching groups', async () => {
    const first = new Subject<EventResponse[]>();
    getEvents
      .mockReturnValueOnce(first)
      .mockReturnValueOnce(of([{ id: 8, groupId: 2, name: 'Cumpleaños' }]));
    const fixture = TestBed.createComponent(DashboardPage);
    fixture.componentInstance.selectGroup(1);
    await fixture.whenStable();
    fixture.componentInstance.selectGroup(2);
    await fixture.whenStable();
    expect(first.observed).toBe(false);
    first.next([{ id: 7, groupId: 1, name: 'Old event' }]);
    expect(fixture.componentInstance.events()[0].name).toBe('Cumpleaños');
  });
  it('distinguishes failed event loading from an empty group and supports retry', async () => {
    getEvents
      .mockReturnValueOnce(throwError(() => new Error('Unavailable')))
      .mockReturnValueOnce(of([]));
    const fixture = TestBed.createComponent(DashboardPage);
    fixture.componentInstance.selectGroup(1);
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain(
      'No hemos podido cargar los eventos',
    );
    expect(fixture.nativeElement.textContent).not.toContain(
      'todavía no tiene eventos',
    );
    fixture.componentInstance.retryEvents();
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain(
      'todavía no tiene eventos',
    );
  });
  it('does not request events when the user has no groups', async () => {
    getMyGroups.mockReturnValue(of([]));
    const fixture = TestBed.createComponent(DashboardPage);
    await fixture.whenStable();
    expect(getEvents).not.toHaveBeenCalled();
    expect(fixture.nativeElement.textContent).toContain('Aún no perteneces');
  });
});
