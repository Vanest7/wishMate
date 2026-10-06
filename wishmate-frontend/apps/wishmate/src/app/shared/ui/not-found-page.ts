import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found-page',
  imports: [MatButtonModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <main class="mx-auto max-w-xl p-8">
      <h1 class="mb-4 text-2xl font-bold">Página no encontrada</h1>
      <p class="mb-4">La dirección no existe o no tiene un formato válido.</p>
      <a mat-button routerLink="/">Volver al inicio</a>
    </main>
  `,
})
export class NotFoundPage {}
