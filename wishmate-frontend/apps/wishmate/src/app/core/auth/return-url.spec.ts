import { describe, expect, it } from 'vitest';
import { safeReturnUrl } from './return-url';

describe('safeReturnUrl', () => {
  it.each([
    '/dashboard',
    '/dashboard?section=wishes',
    '/groups/12/events/34',
    '/groups',
    '/groups/12',
    '/groups/12/members',
    '/reservations',
    '/groups/12/events',
    '/groups/12/events/34/wishes?view=mine#list',
  ])('preserves the private destination %s', (url) =>
    expect(safeReturnUrl(url)).toBe(url),
  );
  it.each([
    null,
    '',
    'https://example.com',
    '//example.com',
    '/auth/login',
    '/groups/../auth/login',
    '/groups/0/events',
    '/groups\\evil',
    '/unknown',
  ])('replaces the invalid destination %s', (url) =>
    expect(safeReturnUrl(url)).toBe('/dashboard'),
  );
});
