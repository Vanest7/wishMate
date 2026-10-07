import { describe, expect, it } from 'vitest';
import { jwtSubject } from './jwt-subject';
describe('JWT UI identity', () => {
  it('extracts the numeric subject without using role claims', () => {
    expect(
      jwtSubject(
        `header.${btoa(JSON.stringify({ sub: '17', role: 'OWNER' }))}.signature`,
      ),
    ).toBe(17);
  });
  it.each([null, '', 'invalid', 'a.!.c', 'a.e30.c'])(
    'rejects malformed tokens: %s',
    (token) => expect(jwtSubject(token)).toBeNull(),
  );
  it.each(['0', '-1', 'NaN', '9007199254740992', 17])(
    'rejects invalid subjects: %s',
    (sub) => {
      expect(jwtSubject(`a.${btoa(JSON.stringify({ sub }))}.c`)).toBeNull();
    },
  );
});
