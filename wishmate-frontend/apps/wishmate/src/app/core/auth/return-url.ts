// Only private application destinations are accepted after authentication.
export function safeReturnUrl(value: string | null): string {
  if (!value || /[\\\s]/.test(value)) return '/groups';
  const pathname = value.split(/[?#]/, 1)[0];
  const id = '[1-9]\\d*';
  const allowed = new RegExp(
    `^/(?:reservations|groups(?:/${id}/events(?:/${id}/wishes)?)?)/?$`,
  );
  return allowed.test(pathname) ? value : '/groups';
}
