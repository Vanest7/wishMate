import { DOCUMENT } from '@angular/common';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { ApiConfiguration, AuthenticationService } from '@wishmate/api-client';
import { catchError, switchMap, throwError } from 'rxjs';
import { CsrfService } from './csrf.service';
import { SessionService } from './session.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const document = inject(DOCUMENT);
  const config = inject(ApiConfiguration);
  const base = new URL(config.rootUrl || '/', document.baseURI);
  const url = new URL(request.url, document.baseURI);
  const rootPath = base.pathname.replace(/\/$/, '');
  if (
    url.origin !== base.origin ||
    !url.pathname.startsWith(`${rootPath}/api/`)
  ) {
    return next(request);
  }

  const path = url.pathname.slice(rootPath.length);
  const isPublic = [
    AuthenticationService.LoginPath,
    AuthenticationService.RegisterPath,
    AuthenticationService.GetCsrfTokenPath,
  ].includes(path);
  const session = inject(SessionService);
  const token = isPublic ? null : session.getAccessToken();
  const csrf = inject(CsrfService);
  const prepared = request.clone({
    withCredentials: true,
    ...(token ? { setHeaders: { Authorization: `Bearer ${token}` } } : {}),
  });
  const unsafe = !['GET', 'HEAD', 'OPTIONS'].includes(request.method);
  const exempt =
    request.method === 'POST' && path === AuthenticationService.RegisterPath;
  const response =
    unsafe && !exempt
      ? csrf
          .acquire()
          .pipe(
            switchMap(({ headerName, token: csrfToken }) =>
              next(prepared.clone({ setHeaders: { [headerName]: csrfToken } })),
            ),
          )
      : next(prepared);

  return response.pipe(
    catchError((error: unknown) => {
      if (
        error instanceof HttpErrorResponse &&
        error.status === 401 &&
        token &&
        session.getAccessToken() === token
      ) {
        session.clear();
      }
      return throwError(() => error);
    }),
  );
};
