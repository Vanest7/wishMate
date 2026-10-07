import { Routes } from '@angular/router';

export const reservationsRoutes: Routes = [
  {
    path: 'new/:groupId/:eventId/:wishId',
    title: 'Reservar un regalo | WishMate',
    loadComponent: () =>
      import('./pages/reservation-create-page').then(
        (m) => m.ReservationCreatePage,
      ),
  },
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
