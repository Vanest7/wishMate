import { HttpErrorResponse } from '@angular/common/http';
import { describe, expect, it } from 'vitest';
import { apiErrorMessage, classifyApiError } from './api-error';

const messages = {
  network: 'network',
  validation: 'validation',
  forbidden: 'forbidden',
  missing: 'missing',
  conflict: 'conflict',
  generic: 'generic',
};
describe('API error classification', () => {
  it.each([
    [0, 'network'],
    [400, 'validation'],
    [401, 'unauthorized'],
    [403, 'forbidden'],
    [404, 'missing'],
    [409, 'conflict'],
    [500, 'generic'],
  ] as const)(
    'classifies status %s without trusting the response body',
    (status, expected) => {
      expect(
        classifyApiError(
          new HttpErrorResponse({ status, error: '<html>Failure</html>' }),
        ),
      ).toBe(expected);
    },
  );
  it('preserves feature-specific authentication handling and unknown-error fallback', () => {
    const error = new HttpErrorResponse({ status: 401 });
    expect(apiErrorMessage(error, messages)).toBe('generic');
    expect(
      apiErrorMessage(error, { ...messages, unauthorized: 'sign in' }),
    ).toBe('sign in');
    expect(apiErrorMessage({ status: 403 }, messages)).toBe('generic');
    expect(apiErrorMessage(null, messages)).toBe('generic');
  });
});
