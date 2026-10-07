import {
  HttpClient,
  provideHttpClient,
  withInterceptors,
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideApiConfiguration } from '@wishmate/api-client';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthService } from './auth.service';
import { authInterceptor } from '../../../core/auth/auth.interceptor';
import { SessionService } from '../../../core/auth/session.service';

const baseUrl = 'http://localhost:8080';

describe('Authentication HTTP integration', () => {
  let http: HttpTestingController;
  let auth: AuthService;
  let session: SessionService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideApiConfiguration(baseUrl),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpTestingController);
    auth = TestBed.inject(AuthService);
    session = TestBed.inject(SessionService);
  });
  afterEach(() => http.verify());

  it('acquires CSRF before login, preserves the password and stores the session', () => {
    const next = vi.fn();
    auth
      .login({ email: 'test@example.com', password: ' pass word ' })
      .subscribe(next);
    http.expectNone(`${baseUrl}/api/auth/login`);
    const csrf = http.expectOne(`${baseUrl}/api/auth/csrf`);
    expect(csrf.request.withCredentials).toBe(true);
    expect(csrf.request.headers.has('Authorization')).toBe(false);
    csrf.flush({ headerName: 'X-XSRF-TOKEN', token: 'csrf-test' });
    const login = http.expectOne(`${baseUrl}/api/auth/login`);
    expect(login.request.method).toBe('POST');
    expect(login.request.body).toEqual({
      email: 'test@example.com',
      password: ' pass word ',
    });
    expect(login.request.withCredentials).toBe(true);
    expect(login.request.headers.get('X-XSRF-TOKEN')).toBe('csrf-test');
    login.flush({
      accessToken: 'access-test',
      tokenType: 'Bearer',
      expiresIn: 900,
    });
    expect(session.getAccessToken()).toBe('access-test');
    expect(next).toHaveBeenCalledOnce();
  });

  it('registers without CSRF and does not create a session', () => {
    auth
      .register({
        userName: 'Test',
        handle: 'test_user',
        email: 'test@example.com',
        password: ' pass ',
      })
      .subscribe();
    http.expectNone(`${baseUrl}/api/auth/csrf`);
    const request = http.expectOne(`${baseUrl}/api/auth/register`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      userName: 'Test',
      handle: 'test_user',
      email: 'test@example.com',
      password: ' pass ',
    });
    request.flush({ id: 1 }, { status: 201, statusText: 'Created' });
    expect(session.isAuthenticated()).toBe(false);
  });

  it('does not send tokens or credentials to third-party or lookalike URLs', () => {
    session.start('private-token', 900);
    for (const url of [
      'https://example.com/api/groups',
      'http://localhost.evil.example:8080/api/groups',
      `${baseUrl}/api-other/groups`,
    ]) {
      TestBed.inject(HttpClient).post(url, {}).subscribe();
      const request = http.expectOne(url);
      expect(request.request.withCredentials).toBe(false);
      expect(request.request.headers.has('Authorization')).toBe(false);
      expect(request.request.headers.has('X-XSRF-TOKEN')).toBe(false);
      request.flush({});
    }
    http.expectNone(`${baseUrl}/api/auth/csrf`);
  });

  it('adds Bearer and CSRF to an authenticated write', () => {
    session.start('private-token', 900);
    TestBed.inject(HttpClient)
      .post(`${baseUrl}/api/groups`, { name: 'Test' })
      .subscribe();
    http
      .expectOne(`${baseUrl}/api/auth/csrf`)
      .flush({ headerName: 'X-XSRF-TOKEN', token: 'csrf-test' });
    const request = http.expectOne(`${baseUrl}/api/groups`);
    expect(request.request.headers.get('Authorization')).toBe(
      'Bearer private-token',
    );
    expect(request.request.headers.get('X-XSRF-TOKEN')).toBe('csrf-test');
    request.flush({});
  });

  it('does not retry a rejected login or create a session', () => {
    const error = vi.fn();
    auth
      .login({ email: 'test@example.com', password: 'wrong' })
      .subscribe({ error });
    http
      .expectOne(`${baseUrl}/api/auth/csrf`)
      .flush({ headerName: 'X-XSRF-TOKEN', token: 'csrf-test' });
    http
      .expectOne(`${baseUrl}/api/auth/login`)
      .flush('', { status: 401, statusText: 'Unauthorized' });
    expect(error).toHaveBeenCalledOnce();
    expect(session.isAuthenticated()).toBe(false);
    http.expectNone(`${baseUrl}/api/auth/login`);
  });

  it('rejects malformed success payloads', () => {
    const error = vi.fn();
    auth
      .login({ email: 'test@example.com', password: 'test' })
      .subscribe({ error });
    http
      .expectOne(`${baseUrl}/api/auth/csrf`)
      .flush({ headerName: 'X-XSRF-TOKEN', token: 'csrf-test' });
    http
      .expectOne(`${baseUrl}/api/auth/login`)
      .flush({ tokenType: 'Bearer', expiresIn: 900 });
    expect(error).toHaveBeenCalledOnce();
    expect(session.isAuthenticated()).toBe(false);
  });

  it('clears the current session on a private 401 without retrying', () => {
    session.start('expired-token', 900);
    TestBed.inject(HttpClient)
      .get(`${baseUrl}/api/groups`)
      .subscribe({ error: () => undefined });
    http
      .expectOne(`${baseUrl}/api/groups`)
      .flush(null, { status: 401, statusText: 'Unauthorized' });
    expect(session.isAuthenticated()).toBe(false);
  });

  it('shares an in-flight CSRF request and reacquires it for a later write', () => {
    const client = TestBed.inject(HttpClient);
    client.post(`${baseUrl}/api/groups`, {}).subscribe();
    client.post(`${baseUrl}/api/groups`, {}).subscribe();
    http
      .expectOne(`${baseUrl}/api/auth/csrf`)
      .flush({ headerName: 'X-XSRF-TOKEN', token: 'first' });
    for (const request of http.match(`${baseUrl}/api/groups`))
      request.flush({});
    client.post(`${baseUrl}/api/groups`, {}).subscribe();
    http
      .expectOne(`${baseUrl}/api/auth/csrf`)
      .flush({ headerName: 'X-XSRF-TOKEN', token: 'second' });
    const later = http.expectOne(`${baseUrl}/api/groups`);
    expect(later.request.headers.get('X-XSRF-TOKEN')).toBe('second');
    later.flush({});
  });
});
