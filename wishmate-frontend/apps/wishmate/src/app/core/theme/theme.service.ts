import { DOCUMENT } from '@angular/common';
import { DestroyRef, Injectable, inject, signal } from '@angular/core';

export type ThemePreference = 'system' | 'light' | 'dark';

const STORAGE_KEY = 'wishmate.theme';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly document = inject(DOCUMENT);
  private readonly destroyRef = inject(DestroyRef);
  private readonly browserWindow = this.document.defaultView;
  private readonly systemTheme = this.browserWindow?.matchMedia(
    '(prefers-color-scheme: dark)',
  );

  private readonly preferenceState = signal<ThemePreference>('system');

  readonly preference = this.preferenceState.asReadonly();

  constructor() {
    this.preferenceState.set(this.readPreference());
    this.applyTheme();

    const handleSystemChange = (): void => {
      if (this.preferenceState() === 'system') {
        this.applyTheme();
      }
    };

    this.systemTheme?.addEventListener('change', handleSystemChange);

    this.destroyRef.onDestroy(() => {
      this.systemTheme?.removeEventListener('change', handleSystemChange);
    });
  }

  setPreference(preference: ThemePreference): void {
    this.preferenceState.set(preference);
    this.applyTheme();

    try {
      this.browserWindow?.localStorage.setItem(STORAGE_KEY, preference);
    } catch {
      // Keep the selected theme for this session if storage is unavailable.
    }
  }

  private readPreference(): ThemePreference {
    try {
      const stored = this.browserWindow?.localStorage.getItem(STORAGE_KEY);

      if (stored === 'light' || stored === 'dark') {
        return stored;
      }
    } catch {
      // Follow the system preference if storage is unavailable.
    }

    return 'system';
  }

  private applyTheme(): void {
    const preference = this.preferenceState();
    const resolvedTheme =
      preference === 'system'
        ? this.systemTheme?.matches
          ? 'dark'
          : 'light'
        : preference;

    this.document.documentElement.setAttribute('data-theme', resolvedTheme);
  }
}
