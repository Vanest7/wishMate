import {
  ChangeDetectionStrategy,
  Component,
  effect,
  inject,
} from '@angular/core';
import {
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet,
} from '@angular/router';
import { SessionService } from '../core/auth/session.service';

@Component({
  selector: 'app-authenticated-layout',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrl: './authenticated-layout.scss',
  template: `
    <a class="skip-link" href="#main-content">Saltar al contenido</a>
    <header class="header">
      <div class="header-content">
        <a routerLink="/dashboard" aria-label="Ir a mi panel" class="brand">
          <img
            src="/WishMate_ regalos compartidos con corazón.png"
            alt="WishMate"
            class="brand-logo"
          />
        </a>

        <nav class="navigation" aria-label="Navegación principal">
          <a
            routerLink="/dashboard"
            routerLinkActive="is-active"
            ariaCurrentWhenActive="page"
            >Mi panel</a
          >
          <a
            routerLink="/groups"
            routerLinkActive="is-active"
            ariaCurrentWhenActive="page"
            >Mis grupos</a
          >
          <a
            routerLink="/reservations"
            routerLinkActive="is-active"
            ariaCurrentWhenActive="page"
            >Mis reservas</a
          >
          <button type="button" (click)="logout()">Cerrar sesión</button>
        </nav>
      </div>
    </header>
    <main id="main-content" tabindex="-1" class="main">
      <router-outlet />
    </main>
  `,
})
export class AuthenticatedLayout {
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);

  constructor() {
    effect(() => {
      if (!this.session.isAuthenticated()) {
        void this.router.navigate(['/auth/login'], {
          queryParams: { returnUrl: this.router.url },
          replaceUrl: true,
        });
      }
    });
  }

  logout(): void {
    this.session.clear();
  }
}
