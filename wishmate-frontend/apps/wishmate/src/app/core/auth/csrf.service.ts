import { inject, Injectable } from '@angular/core';
import { AuthenticationService } from '@wishmate/api-client';
import { finalize, map, Observable, shareReplay } from 'rxjs';

interface CsrfCredentials {
  headerName: string;
  token: string;
}

@Injectable({ providedIn: 'root' })
export class CsrfService {
  private readonly api = inject(AuthenticationService);
  private pending: Observable<CsrfCredentials> | undefined;

  acquire(): Observable<CsrfCredentials> {
    if (!this.pending) {
      this.pending = this.api.getCsrfToken().pipe(
        map((response): CsrfCredentials => {
          if (response.headerName !== 'X-XSRF-TOKEN' || !response.token) {
            throw new Error('Invalid CSRF response.');
          }
          return { headerName: response.headerName, token: response.token };
        }),
        finalize(() => {
          this.pending = undefined;
        }),
        shareReplay({ bufferSize: 1, refCount: true }),
      );
    }
    return this.pending;
  }
}
