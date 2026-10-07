import { Routes } from '@angular/router';
import { authGuard, authenticatedChildGuard } from './core/auth/auth.guard';
import { resourceIdGuard } from './core/routing/resource-id.guard';

export const appRoutes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  {
    path: 'auth',
    loadComponent: () =>
      import('./layout/public-layout').then((m) => m.PublicLayout),
    loadChildren: () =>
      import('./features/auth/auth.routes').then((m) => m.authRoutes),
  },
  {
    path: 'not-found',
    title: 'Página no encontrada | WishMate',
    loadComponent: () =>
      import('./shared/ui/not-found-page').then((m) => m.NotFoundPage),
  },
  {
    path: '',
    canActivate: [authGuard],
    canActivateChild: [authenticatedChildGuard],
    loadComponent: () =>
      import('./layout/authenticated-layout').then(
        (m) => m.AuthenticatedLayout,
      ),
    children: [
      {
        path: 'dashboard',
        title: 'Mi panel | WishMate',
        loadComponent: () =>
          import('./features/dashboard/pages/dashboard-page').then(
            (m) => m.DashboardPage,
          ),
      },
      {
        path: 'groups/:groupId/events/:eventId/wishes',
        canActivate: [resourceIdGuard],
        runGuardsAndResolvers: 'paramsChange',
        loadChildren: () =>
          import('./features/wishes/wishes.routes').then((m) => m.wishesRoutes),
      },
      {
        path: 'groups',
        loadChildren: () =>
          import('./features/groups/groups.routes').then((m) => m.groupsRoutes),
      },
      {
        path: 'reservations',
        loadChildren: () =>
          import('./features/reservations/reservations.routes').then(
            (m) => m.reservationsRoutes,
          ),
      },
    ],
  },
  { path: '**', redirectTo: 'not-found' },
];
