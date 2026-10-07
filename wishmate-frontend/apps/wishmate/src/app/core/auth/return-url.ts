// Only private application destinations are accepted after authentication.
export function safeReturnUrl(value: string | null): string {
  if (!value || /[\\\s]/.test(value)) return '/dashboard';
  const pathname = value.split(/[?#]/, 1)[0];
  const id = '[1-9]\\d*';
  const allowed = new RegExp(
    `^/(?:dashboard|reservations(?:/new/${id}/${id}/${id})?|groups(?:/${id}(?:/members|/events(?:/${id}(?:/wishes)?)?)?)?)/?$`,
  );
  return allowed.test(pathname) ? value : '/dashboard';
}
