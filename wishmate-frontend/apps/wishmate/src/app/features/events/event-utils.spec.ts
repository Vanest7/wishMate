import { describe, expect, it } from 'vitest';
import {
  canEventAction,
  formatEventDate,
  validDateOnly,
  validEvent,
} from './event-utils';

describe('Event rules', () => {
  it.each([
    ['DRAFT', true, true, false, false],
    ['ACTIVE', true, false, true, false],
    ['CLOSED', false, false, false, true],
    ['ARCHIVED', false, false, false, false],
    [undefined, false, false, false, false],
  ] as const)(
    'derives actions for %s',
    (status, edit, activate, close, archive) => {
      expect(
        ['edit', 'activate', 'close', 'archive'].map((action) =>
          canEventAction(
            status,
            action as 'edit' | 'activate' | 'close' | 'archive',
          ),
        ),
      ).toEqual([edit, activate, close, archive]);
    },
  );
  it('rejects incomplete events and events from another group', () => {
    const event = {
      id: 2,
      groupId: 1,
      name: 'Birthday',
      status: 'ACTIVE' as const,
    };
    expect(validEvent(event, 1)).toBe(true);
    expect(validEvent(event, 3)).toBe(false);
    expect(validEvent({ ...event, id: 0 }, 1)).toBe(false);
    expect(validEvent({ ...event, status: undefined }, 1)).toBe(false);
    expect(validEvent({ ...event, name: ' ' }, 1)).toBe(false);
  });
  it.each([
    '2026-02-29',
    '2026-02-31',
    '2026-13-01',
    '0000-01-01',
    '2026-1-1',
    'bad',
  ])('rejects invalid date %s', (value) => {
    expect(validDateOnly(value)).toBe(false);
  });
  it('preserves calendar dates without timezone conversion', () => {
    expect(validDateOnly('2028-02-29')).toBe(true);
    expect(validDateOnly('')).toBe(true);
    expect(formatEventDate('2026-10-07')).toBe('07/10/2026');
    expect(formatEventDate(null)).toBe('—');
  });
});
