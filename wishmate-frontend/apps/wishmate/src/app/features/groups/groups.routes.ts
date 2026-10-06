import { Routes } from '@angular/router';

export const groupsRoutes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    title: 'Mis grupos | WishMate',
    loadComponent: () =>
      import('./pages/group-list-page').then((m) => m.GroupListPage),
  },
];
