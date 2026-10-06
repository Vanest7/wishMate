import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const resourceIdGuard: CanActivateFn = (route) => {
  const valid = ['groupId', 'eventId'].every((name) => {
    const value = route.paramMap.get(name);
    return (
      value === null ||
      (/^[1-9]\d*$/.test(value) && Number.isSafeInteger(Number(value)))
    );
  });
  return valid || inject(Router).createUrlTree(['/not-found']);
};
