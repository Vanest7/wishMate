import { DOCUMENT } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  inject,
  signal,
} from '@angular/core';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ThemeService } from '../core/theme/theme.service';

@Component({
  selector: 'app-theme-selector',
  imports: [MatTooltipModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div
      class="theme-selector"
      role="group"
      aria-label="Tema de la aplicación"
      [class.is-dark]="isDark()"
    >
      <span class="selection-indicator" aria-hidden="true"></span>

      <button
        type="button"
        aria-label="Modo claro"
        matTooltip="Modo claro"
        [attr.aria-pressed]="!isDark()"
        (click)="theme.setPreference('light')"
      >
        <svg
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="1.8"
          stroke-linecap="round"
          aria-hidden="true"
          focusable="false"
        >
          <circle cx="12" cy="12" r="4" />
          <path
            d="M12 2v2m0 16v2M2 12h2m16 0h2
               M4.93 4.93l1.42 1.42m11.3 11.3 1.42 1.42
               M4.93 19.07l1.42-1.42m11.3-11.3 1.42-1.42"
          />
        </svg>
      </button>

      <button
        type="button"
        aria-label="Modo oscuro"
        matTooltip="Modo oscuro"
        [attr.aria-pressed]="isDark()"
        (click)="theme.setPreference('dark')"
      >
        <svg
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="1.8"
          stroke-linecap="round"
          stroke-linejoin="round"
          aria-hidden="true"
          focusable="false"
        >
          <path
            d="M20.5 13.1A8.5 8.5 0 0 1 10.9 3.5
               8.5 8.5 0 1 0 20.5 13.1Z"
          />
        </svg>
      </button>
    </div>
  `,
  styles: `
    :host {
      display: inline-flex;
      flex-shrink: 0;
    }

    .theme-selector {
      position: relative;
      display: inline-grid;
      grid-template-columns: repeat(2, 44px);
      gap: 4px;
      padding: 4px;
      border: 1px solid var(--mat-sys-outline-variant);
      border-radius: 999px;
      background: var(--mat-sys-surface-container);
      isolation: isolate;
    }

    .selection-indicator {
      position: absolute;
      top: 4px;
      left: 4px;
      width: 44px;
      height: 44px;
      border-radius: 50%;
      background: var(--mat-sys-secondary-container);
      transition: transform 180ms ease;
      z-index: -1;
    }

    .is-dark .selection-indicator {
      transform: translateX(48px);
    }

    button {
      display: grid;
      place-items: center;
      width: 44px;
      height: 44px;
      padding: 0;
      border: 0;
      border-radius: 50%;
      background: transparent;
      color: var(--mat-sys-on-surface-variant);
      cursor: pointer;
      transition: color 180ms ease;
    }

    button[aria-pressed='true'] {
      color: var(--mat-sys-on-secondary-container);
    }

    button:hover {
      background: color-mix(in srgb, var(--mat-sys-on-surface) 8%, transparent);
    }

    button:focus-visible {
      outline: 2px solid var(--mat-sys-secondary);
      outline-offset: 2px;
    }

    svg {
      width: 22px;
      height: 22px;
    }

    @media (prefers-reduced-motion: reduce) {
      .selection-indicator,
      button {
        transition: none;
      }
    }
  `,
})
export class ThemeSelector {
  readonly theme = inject(ThemeService);

  private readonly document = inject(DOCUMENT);
  private readonly destroyRef = inject(DestroyRef);
  private readonly systemTheme = this.document.defaultView?.matchMedia(
    '(prefers-color-scheme: dark)',
  );
  private readonly systemIsDark = signal(this.systemTheme?.matches ?? false);

  readonly isDark = computed(() => {
    const preference = this.theme.preference();

    return preference === 'system'
      ? this.systemIsDark()
      : preference === 'dark';
  });

  constructor() {
    const handleSystemChange = (event: MediaQueryListEvent): void => {
      this.systemIsDark.set(event.matches);
    };

    this.systemTheme?.addEventListener('change', handleSystemChange);

    this.destroyRef.onDestroy(() => {
      this.systemTheme?.removeEventListener('change', handleSystemChange);
    });
  }
}
