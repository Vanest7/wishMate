import {
  ChangeDetectionStrategy,
  Component,
  inject,
  Signal,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { ROUTER_OUTLET_DATA, RouterLink } from '@angular/router';
import { GroupSectionContext } from '../../groups/pages/group-section-context';

@Component({
  selector: 'app-event-list-page',
  imports: [MatButtonModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h1 class="mb-4 text-2xl font-bold">Eventos</h1>
    <p class="mb-4">Grupo {{ context().group.name }}</p>
    <a mat-button routerLink="/groups">Volver a grupos</a>
    <p>
      Aquí podrás consultar los eventos del grupo. El listado está pendiente de
      implementación.
    </p>
  `,
})
export class EventListPage {
  readonly context = inject<Signal<GroupSectionContext>>(ROUTER_OUTLET_DATA);
}
