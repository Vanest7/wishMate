import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-event-list-page',
  imports: [MatButtonModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h1 class="mb-4 text-2xl font-bold">Eventos</h1>
    <p class="mb-4">Grupo {{ groupId() }}</p>
    <a mat-button routerLink="/groups">Volver a grupos</a>
    <p>
      Aquí podrás consultar los eventos del grupo. El listado está pendiente de
      implementación.
    </p>
  `,
})
export class EventListPage {
  readonly groupId = input.required<string>();
}
