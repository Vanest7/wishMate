import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-group-list-page',

  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h1 class="mb-4 text-2xl font-bold">Mis grupos</h1>

    <p>
      Aquí podrás consultar tus grupos. El listado se implementará en la tarea
      de grupos.
    </p>
  `,
})
export class GroupListPage {}
