// This identifies the UI user only. Authorization belongs to the backend.
export function jwtSubject(token: string | null): number | null {
  if (!token) return null;
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    const payload: unknown = JSON.parse(
      atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')),
    );
    if (
      !payload ||
      typeof payload !== 'object' ||
      !('sub' in payload) ||
      typeof payload.sub !== 'string' ||
      !/^[1-9]\d*$/.test(payload.sub)
    )
      return null;
    const id = Number(payload.sub);
    return Number.isSafeInteger(id) ? id : null;
  } catch {
    return null;
  }
}
