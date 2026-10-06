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
  selector: 'app-register-page',
  imports: [MatButtonModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h1 class="mb-4 text-2xl font-bold">Crear cuenta</h1>
    <p class="mb-4">
      El formulario de registro está pendiente de implementación.
    </p>
    <a
      mat-button
      routerLink="/auth/login"
      [queryParams]="{ returnUrl: destination() }"
    >
      Volver al acceso
    </a>
  `,
})
export class RegisterPage {
  readonly returnUrl = input<string | null>(null);
  readonly destination = computed(() => safeReturnUrl(this.returnUrl()));
}
