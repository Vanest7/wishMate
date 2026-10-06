import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  input,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { safeReturnUrl } from '../../../core/auth/return-url';
import { authContent } from '../auth.content';
import { applyAuthError, nonBlank } from '../auth-form.utils';
import { AuthService } from '../data-access/auth.service';

@Component({
  selector: 'app-login-page',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    RouterLink,
  ],
  templateUrl: './login-page.html',
  styleUrl: './auth-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly builder = inject(FormBuilder).nonNullable;
  readonly content = authContent;
  readonly returnUrl = input<string | null>(null);
  readonly destination = computed(() => safeReturnUrl(this.returnUrl()));
  readonly pending = signal(false);
  readonly hidePassword = signal(true);
  readonly errorMessage = signal('');

  readonly form = this.builder.group({
    email: ['', [nonBlank, Validators.email]],
    password: ['', [nonBlank]],
  });

  submit(): void {
    if (this.pending()) return;
    this.form.controls.email.setValue(this.form.controls.email.value.trim());

    for (const control of Object.values(this.form.controls))
      control.updateValueAndValidity();
    this.form.updateValueAndValidity();
    this.errorMessage.set('');
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    this.pending.set(true);
    this.form.disable();
    this.auth
      .login({
        email: value.email,
        password: value.password,
      })
      .pipe(
        finalize(() => {
          this.pending.set(false);
          if (this.form.disabled) this.form.enable();
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.form.reset();
          void this.router.navigateByUrl(this.destination(), {
            replaceUrl: true,
          });
        },
        error: (error: unknown) => {
          this.form.enable();
          this.errorMessage.set(applyAuthError(error, this.form, 'login'));
        },
      });
  }
}
