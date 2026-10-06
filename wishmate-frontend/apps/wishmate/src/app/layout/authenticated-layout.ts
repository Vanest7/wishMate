import {
  ChangeDetectionStrategy,
  Component,
  effect,
  inject,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import {
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet,
} from '@angular/router';
import { SessionService } from '../core/auth/session.service';

@Component({
  selector: 'app-authenticated-layout',
  imports: [MatButtonModule, RouterLink, RouterLinkActive, RouterOutlet],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <a class="sr-only focus:not-sr-only" href="#main-content"
      >Saltar al contenido</a
    >
    <header
      class="flex flex-wrap items-center justify-between gap-4 border-b p-4"
    >
      <a routerLink="/groups" class="text-xl font-bold">WishMate</a>
      <nav aria-label="Navegación principal" class="flex flex-wrap gap-2">
        <a
          mat-button
          routerLink="/groups"
          routerLinkActive="font-bold"
          ariaCurrentWhenActive="page"
          >Mis grupos</a
        >
        <a
          mat-button
          routerLink="/reservations"
          routerLinkActive="font-bold"
          ariaCurrentWhenActive="page"
          >Mis reservas</a
        >
        <button mat-button type="button" (click)="logout()">
          Cerrar sesión
        </button>
      </nav>
    </header>
    <main id="main-content" tabindex="-1" class="mx-auto max-w-5xl p-6">
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
