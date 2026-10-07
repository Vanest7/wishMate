import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import {
  provideRouter,
  Router,
  withComponentInputBinding,
  withRouterConfig,
} from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { of } from 'rxjs';
import {
  GroupsService,
  EventsService,
  GroupMembersService,
  WishesService,
} from '@wishmate/api-client';
import { appRoutes } from './app.routes';
import { SessionService } from './core/auth/session.service';

// Session setup belongs only in tests; production has no login bypass.
describe('Application navigation', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideRouter(
          appRoutes,
          withComponentInputBinding(),
          withRouterConfig({ paramsInheritanceStrategy: 'always' }),
        ),
      ],
    });
  });

  it('preserves a private deep link when a new session has no token', async () => {
    const url = '/groups/12/events/34/wishes?view=mine#list';
    const harness = await RouterTestingHarness.create(url);
    const router = TestBed.inject(Router);
    expect(router.parseUrl(router.url).queryParams['returnUrl']).toBe(url);
    expect(router.url.startsWith('/auth/login')).toBe(true);
    expect(harness.routeNativeElement?.textContent).toContain('Iniciar sesión');
  });

  it('loads parent IDs but displays group and event names on navigation', async () => {
    const getWishesByEvent = vi.fn(() => of([]));
    TestBed.configureTestingModule({
      providers: [
        {
          provide: GroupsService,
          useValue: {
            getGroup: ({ groupId }: { groupId: number }) =>
              of({ id: groupId, name: groupId === 12 ? 'Familia' : 'Amigos' }),
          },
        },
        {
          provide: EventsService,
          useValue: {
            getEvent: ({
              groupId,
              eventId,
            }: {
              groupId: number;
              eventId: number;
            }) =>
              of({
                id: eventId,
                groupId,
                name: eventId === 34 ? 'Cumpleaños' : 'Navidad',
                status: 'ACTIVE',
              }),
          },
        },
        {
          provide: GroupMembersService,
          useValue: {
            getMembers: () =>
              of([{ userId: 7, userName: 'Ana', role: 'MEMBER' }]),
          },
        },
        { provide: WishesService, useValue: { getWishesByEvent } },
      ],
    });
    TestBed.inject(SessionService).start(
      `a.${btoa(JSON.stringify({ sub: '7' }))}.c`,
      900,
    );
    const harness = await RouterTestingHarness.create(
      '/groups/12/events/34/wishes',
    );
    await harness.fixture.whenStable();
    harness.detectChanges();
    expect(harness.routeNativeElement?.textContent).toContain('Familia');
    expect(harness.routeNativeElement?.textContent).toContain('Cumpleaños');
    expect(harness.routeNativeElement?.textContent).not.toContain('Grupo 12');
    expect(getWishesByEvent).toHaveBeenCalledWith({ groupId: 12, eventId: 34 });
    await harness.navigateByUrl('/groups/56/events/78/wishes');
    await harness.fixture.whenStable();
    harness.detectChanges();
    expect(harness.routeNativeElement?.textContent).toContain('Amigos');
    expect(harness.routeNativeElement?.textContent).toContain('Navidad');
    expect(harness.routeNativeElement?.textContent).not.toContain('Familia');
    expect(harness.routeNativeElement?.textContent).not.toContain('Evento 78');
    expect(getWishesByEvent).toHaveBeenCalledWith({ groupId: 56, eventId: 78 });
  });

  it('rejects malformed resource IDs without showing a resource page', async () => {
    TestBed.inject(SessionService).start('test-access-token', 900);
    const harness = await RouterTestingHarness.create('/groups/0/events');
    expect(TestBed.inject(Router).url).toBe('/not-found');
    expect(harness.routeNativeElement?.textContent).toContain(
      'Página no encontrada',
    );
  });

  it('shows the public not-found page for an unknown URL', async () => {
    const harness = await RouterTestingHarness.create('/does-not-exist');
    expect(TestBed.inject(Router).url).toBe('/not-found');
    expect(harness.routeNativeElement?.textContent).toContain(
      'Página no encontrada',
    );
  });

  it('protects child navigation after an expired session', async () => {
    const session = TestBed.inject(SessionService);
    session.start('test-access-token', 900);
    const harness = await RouterTestingHarness.create('/groups');
    session.clear();
    await harness.navigateByUrl('/reservations');
    expect(TestBed.inject(Router).url.startsWith('/auth/login')).toBe(true);
    expect(harness.routeNativeElement?.textContent).not.toContain(
      'Cerrar sesión',
    );
  });

  it('keeps the destination while moving between login and registration', async () => {
    const harness = await RouterTestingHarness.create(
      '/auth/register?returnUrl=%2Fgroups%2F12%2Fevents',
    );
    const link = harness.routeNativeElement?.querySelector(
      'a[href*="/auth/login"]',
    );
    expect(link?.getAttribute('href')).toContain(
      'returnUrl=%2Fgroups%2F12%2Fevents',
    );
  });
});
