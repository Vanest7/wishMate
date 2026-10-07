import { Routes } from '@angular/router';
import { resourceIdGuard } from '../../core/routing/resource-id.guard';

export const eventsRoutes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    title: 'Eventos | WishMate',
    loadComponent: () =>
      import('./pages/event-list-page').then((m) => m.EventListPage),
  },
  {
    path: ':eventId',
    title: 'Evento | WishMate',
    canActivate: [resourceIdGuard],
    runGuardsAndResolvers: 'paramsChange',
    loadComponent: () =>
      import('./pages/event-detail-page').then((m) => m.EventDetailPage),
  },
];
