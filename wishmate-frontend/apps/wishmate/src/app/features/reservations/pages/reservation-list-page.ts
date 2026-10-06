import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-reservation-list-page',

  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h1 class="mb-4 text-2xl font-bold">Mis reservas</h1>

    <p>
      Aquí podrás consultar tus reservas. El listado está pendiente de
      implementación.
    </p>
  `,
})
export class ReservationListPage {}
