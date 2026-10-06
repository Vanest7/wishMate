import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';
import { safeReturnUrl } from '../../../core/auth/return-url';

@Component({
  selector: 'app-login-page',
  imports: [MatButtonModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h1 class="mb-4 text-2xl font-bold">Iniciar sesión</h1>
    <p class="mb-4">
      El formulario de acceso está pendiente de implementación.
    </p>
    <p class="mb-4">
      Después de iniciar sesión volverás a: {{ destination() }}
    </p>
    <a
      mat-button
      routerLink="/auth/register"
      [queryParams]="{ returnUrl: destination() }"
    >
      Ir al registro
    </a>
  `,
})
export class LoginPage {
  readonly returnUrl = input<string | null>(null);
  readonly destination = computed(() => safeReturnUrl(this.returnUrl()));
}
