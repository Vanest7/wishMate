import { Routes } from '@angular/router';

export const authRoutes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  {
    path: 'login',
    title: 'Iniciar sesión | WishMate',
    loadComponent: () => import('./pages/login-page').then((m) => m.LoginPage),
  },
  {
    path: 'register',
    title: 'Registrarse | WishMate',
    loadComponent: () =>
      import('./pages/register-page').then((m) => m.RegisterPage),
  },
];
