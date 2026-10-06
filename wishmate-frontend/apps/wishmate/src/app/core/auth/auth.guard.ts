import { inject } from '@angular/core';
import { CanActivateChildFn, CanActivateFn, Router } from '@angular/router';
import { SessionService } from './session.service';

export const authGuard: CanActivateFn = (_route, state) => {
  return inject(SessionService).getAccessToken() !== null
    ? true
    : inject(Router).createUrlTree(['/auth/login'], {
        queryParams: { returnUrl: state.url },
      });
};

export const authenticatedChildGuard: CanActivateChildFn = (route, state) =>
  authGuard(route, state);
