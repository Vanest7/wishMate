import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-wish-list-page',
  imports: [MatButtonModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h1 class="mb-4 text-2xl font-bold">Deseos</h1>
    <p class="mb-4">Grupo {{ groupId() }} · Evento {{ eventId() }}</p>
    <a mat-button [routerLink]="['/groups', groupId(), 'events']"
      >Volver a eventos</a
    >
    <p>
      Aquí podrás consultar los deseos del evento. El listado está pendiente de
      implementación.
    </p>
  `,
})
export class WishListPage {
  readonly groupId = input.required<string>();
  readonly eventId = input.required<string>();
}
