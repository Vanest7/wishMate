import { inject, Injectable } from '@angular/core';
import {
  AuthenticationService,
  LoginRequest,
  RegisterRequest,
} from '@wishmate/api-client';
import { map, Observable, tap } from 'rxjs';
import { SessionService } from '../../../core/auth/session.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(AuthenticationService);
  private readonly session = inject(SessionService);

  login(body: LoginRequest): Observable<void> {
    return this.api.login({ body }).pipe(
      tap((response) => {
        if (
          response.tokenType?.toLowerCase() !== 'bearer' ||
          typeof response.accessToken !== 'string' ||
          typeof response.expiresIn !== 'number'
        ) {
          throw new Error('Invalid login response.');
        }
        this.session.start(response.accessToken, response.expiresIn);
      }),
      map(() => undefined),
    );
  }

  register(body: RegisterRequest): Observable<void> {
    return this.api.register({ body }).pipe(map(() => undefined));
  }
}
