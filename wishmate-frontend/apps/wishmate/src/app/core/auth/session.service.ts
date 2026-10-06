import {
  computed,
  DestroyRef,
  inject,
  Injectable,
  signal,
} from '@angular/core';

interface Session {
  accessToken: string;
  expiresAt: number;
}

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly session = signal<Session | null>(null);
  private expiryTimer: ReturnType<typeof setTimeout> | undefined;
  readonly isAuthenticated = computed(() => this.session() !== null);

  constructor() {
    inject(DestroyRef).onDestroy(() => this.clear());
  }

  // Call only after a successful, validated backend login response.
  start(accessToken: string, expiresIn: number): void {
    this.clear();
    if (!accessToken.trim() || !Number.isFinite(expiresIn) || expiresIn <= 0) {
      throw new Error('Invalid session credentials or lifetime.');
    }
    const expiresAt = Date.now() + expiresIn * 1000;
    if (!Number.isSafeInteger(expiresAt)) {
      throw new Error('Invalid session expiration.');
    }
    this.session.set({ accessToken, expiresAt });
    this.scheduleExpiry();
  }

  getAccessToken(): string | null {
    const current = this.session();
    if (!current || Date.now() >= current.expiresAt) {
      this.clear();
      return null;
    }
    return current.accessToken;
  }

  clear(): void {
    clearTimeout(this.expiryTimer);
    this.expiryTimer = undefined;
    this.session.set(null);
  }

  private scheduleExpiry(): void {
    const current = this.session();
    if (!current) return;
    const remaining = current.expiresAt - Date.now();
    if (remaining <= 0) {
      this.clear();
      return;
    }
    this.expiryTimer = setTimeout(
      () => this.scheduleExpiry(),
      Math.min(remaining, 2_147_483_647),
    );
  }
}
