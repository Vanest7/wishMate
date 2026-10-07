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
    path: ':groupId/members',
    title: 'Miembros | WishMate',
    data: { membersOnly: true },
    canActivate: [resourceIdGuard],
    runGuardsAndResolvers: 'paramsChange',
    loadComponent: () =>
      import('./pages/group-detail-page').then((m) => m.GroupDetailPage),
  },
  {
    path: ':groupId',
    title: 'Grupo | WishMate',
    canActivate: [resourceIdGuard],
    runGuardsAndResolvers: 'paramsChange',
    loadComponent: () =>
      import('./pages/group-detail-page').then((m) => m.GroupDetailPage),
  },
];
