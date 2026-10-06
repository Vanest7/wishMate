import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import {
  provideRouter,
  Router,
  withComponentInputBinding,
  withRouterConfig,
} from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { beforeEach, describe, expect, it } from 'vitest';
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

  it('reads both parent IDs from a direct link and updates them on navigation', async () => {
    TestBed.inject(SessionService).start('test-access-token', 900);
    const harness = await RouterTestingHarness.create(
      '/groups/12/events/34/wishes',
    );
    expect(harness.routeNativeElement?.textContent).toContain(
      'Grupo 12 · Evento 34',
    );
    await harness.navigateByUrl('/groups/56/events/78/wishes');
    expect(harness.routeNativeElement?.textContent).toContain(
      'Grupo 56 · Evento 78',
    );
    expect(harness.routeNativeElement?.textContent).not.toContain('Grupo 12');
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
