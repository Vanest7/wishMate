import { Routes } from '@angular/router';
import { resourceIdGuard } from '../../core/routing/resource-id.guard';

export const groupsRoutes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    title: 'Mis grupos | WishMate',
    loadComponent: () =>
      import('./pages/group-list-page').then((m) => m.GroupListPage),
  },
  {
    path: ':groupId',
    title: 'Grupo | WishMate',
    canActivate: [resourceIdGuard],
    runGuardsAndResolvers: 'paramsChange',
    loadComponent: () =>
      import('./pages/group-detail-page').then((m) => m.GroupDetailPage),
    children: [
      {
        path: '',
        pathMatch: 'full',
        title: 'Grupo | WishMate',
        loadComponent: () =>
          import('./pages/group-overview-page').then(
            (m) => m.GroupOverviewPage,
          ),
      },
      {
        path: 'members',
        title: 'Miembros | WishMate',
        loadComponent: () =>
          import('./pages/group-members-page').then((m) => m.GroupMembersPage),
      },
      {
        path: 'events',
        loadChildren: () =>
          import('../events/events.routes').then((m) => m.eventsRoutes),
      },
    ],
  },
];
