import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { SessionService } from './session.service';

describe('SessionService', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    TestBed.configureTestingModule({});
  });
  afterEach(() => {
    TestBed.resetTestingModule();
    vi.useRealTimers();
  });

  it('starts without a session and does not persist a token across instances', () => {
    const session = TestBed.inject(SessionService);
    expect(session.isAuthenticated()).toBe(false);
    session.start('first-token', 60);
    TestBed.resetTestingModule();
    expect(TestBed.inject(SessionService).getAccessToken()).toBeNull();
  });

  it('expires the session at the supplied deadline', () => {
    const session = TestBed.inject(SessionService);
    session.start('test-token', 60);
    vi.advanceTimersByTime(59_999);
    expect(session.getAccessToken()).toBe('test-token');
    vi.advanceTimersByTime(1);
    expect(session.isAuthenticated()).toBe(false);
    expect(session.getAccessToken()).toBeNull();
  });

  it('checks the deadline even when the expiration timer has not run', () => {
    const session = TestBed.inject(SessionService);
    session.start('test-token', 60);
    vi.setSystemTime(Date.now() + 60_000);
    expect(session.getAccessToken()).toBeNull();
  });

  it('cancels the previous expiration when the account changes', () => {
    const session = TestBed.inject(SessionService);
    session.start('first-token', 1);
    session.start('second-token', 60);
    vi.advanceTimersByTime(1_000);
    expect(session.getAccessToken()).toBe('second-token');
    session.clear();
    expect(session.isAuthenticated()).toBe(false);
  });

  it('rejects invalid credentials and clears any previous session', () => {
    const session = TestBed.inject(SessionService);
    session.start('test-token', 60);
    expect(() => session.start('', 60)).toThrow();
    expect(session.getAccessToken()).toBeNull();
    expect(() => session.start('test-token', 0)).toThrow();
    expect(() => session.start('test-token', Number.NaN)).toThrow();
  });
});
