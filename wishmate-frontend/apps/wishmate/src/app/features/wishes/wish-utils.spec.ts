import { describe, expect, it } from 'vitest';
import {
  canManageWish,
  formatPrice,
  parsePrice,
  safePurchaseUrl,
  validWish,
} from './wish-utils';

describe('Wish rules', () => {
  it.each(['DRAFT', 'CLOSED', 'ARCHIVED'] as const)(
    'blocks owner mutations in %s',
    (status) => {
      expect(
        canManageWish({ userId: 7, eventId: 4 }, { id: 4, status }, 7),
      ).toBe(false);
    },
  );
  it('only grants mutations to the owner in the matching active event', () => {
    const wish = { userId: 7, eventId: 4 };
    expect(canManageWish(wish, { id: 4, status: 'ACTIVE' }, 7)).toBe(true);
    expect(canManageWish(wish, { id: 4, status: 'ACTIVE' }, 8)).toBe(false);
    expect(canManageWish(wish, { id: 5, status: 'ACTIVE' }, 7)).toBe(false);
    expect(canManageWish(wish, { id: 4, status: 'ACTIVE' }, null)).toBe(false);
  });
  it.each(['-1', '100000000', '1,001', '1.000,50', '1e2', 'abc', 'NaN'])(
    'rejects invalid price %s',
    (value) => {
      expect(parsePrice(value)).toBeNull();
    },
  );
  it('accepts comma or dot decimals and preserves zero', () => {
    expect(parsePrice('25,50')).toBe(25.5);
    expect(parsePrice('25.50')).toBe(25.5);
    expect(parsePrice('0')).toBe(0);
    expect(parsePrice('99999999,99')).toBe(99999999.99);
    expect(parsePrice('')).toBeUndefined();
    expect(formatPrice(12345.6)).toBe('12.345,60');
  });
  it.each([
    'javascript:alert(1)',
    'data:text/html,hello',
    'ftp://example.com/file',
    '//example.com',
    'https://user:pass@example.com',
    'invalid',
  ])('rejects unsafe URL %s', (url) => {
    expect(safePurchaseUrl(url)).toBeNull();
  });
  it('accepts explicit HTTP(S) links only', () => {
    expect(safePurchaseUrl('https://example.com/gift')).toBe(
      'https://example.com/gift',
    );
    expect(safePurchaseUrl('http://example.com')).toBe('http://example.com/');
  });
  it('rejects incomplete or cross-event responses', () => {
    const wish = {
      id: 1,
      eventId: 4,
      userId: 7,
      name: 'Book',
      description: 'Hardcover',
    };
    expect(validWish(wish, 4)).toBe(true);
    expect(validWish(wish, 5)).toBe(false);
    expect(validWish({ ...wish, id: undefined }, 4)).toBe(false);
    expect(validWish({ ...wish, userId: undefined }, 4)).toBe(false);
  });
});
