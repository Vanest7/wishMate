import { HttpErrorResponse } from '@angular/common/http';

export type ApiErrorKind =
  | 'network'
  | 'validation'
  | 'unauthorized'
  | 'forbidden'
  | 'missing'
  | 'conflict'
  | 'generic';
export type ApiErrorMessages = Readonly<
  Record<Exclude<ApiErrorKind, 'unauthorized'>, string> & {
    unauthorized?: string;
  }
>;

export function classifyApiError(error: unknown): ApiErrorKind {
  if (!(error instanceof HttpErrorResponse)) return 'generic';
  switch (error.status) {
    case 0:
      return 'network';
    case 400:
      return 'validation';
    case 401:
      return 'unauthorized';
    case 403:
      return 'forbidden';
    case 404:
      return 'missing';
    case 409:
      return 'conflict';
    default:
      return 'generic';
  }
}

export function apiErrorMessage(
  error: unknown,
  messages: ApiErrorMessages,
): string {
  return messages[classifyApiError(error)] ?? messages.generic;
}
