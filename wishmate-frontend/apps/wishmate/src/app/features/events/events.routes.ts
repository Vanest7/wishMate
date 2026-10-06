import { Routes } from '@angular/router';

export const eventsRoutes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    title: 'Eventos | WishMate',
    loadComponent: () =>
      import('./pages/event-list-page').then((m) => m.EventListPage),
  },
];
