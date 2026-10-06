import { Routes } from '@angular/router';

export const wishesRoutes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    title: 'Deseos | WishMate',
    loadComponent: () =>
      import('./pages/wish-list-page').then((m) => m.WishListPage),
  },
];
