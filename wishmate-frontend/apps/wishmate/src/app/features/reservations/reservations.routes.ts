import { Routes } from '@angular/router';

export const reservationsRoutes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    title: 'Mis reservas | WishMate',
    loadComponent: () =>
      import('./pages/reservation-list-page').then(
        (m) => m.ReservationListPage,
      ),
  },
];
