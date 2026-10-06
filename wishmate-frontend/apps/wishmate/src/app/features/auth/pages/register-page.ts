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
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { safeReturnUrl } from '../../../core/auth/return-url';
import { authContent } from '../auth.content';
import {
  applyAuthError,
  nonBlank,
  matchingPasswords,
} from '../auth-form.utils';
import { AuthService } from '../data-access/auth.service';

@Component({
  selector: 'app-register-page',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    RouterLink,
  ],
  templateUrl: './register-page.html',
  styleUrl: './auth-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegisterPage {
  private readonly auth = inject(AuthService);

  private readonly destroyRef = inject(DestroyRef);
  private readonly builder = inject(FormBuilder).nonNullable;
  readonly content = authContent;
  readonly returnUrl = input<string | null>(null);
  readonly destination = computed(() => safeReturnUrl(this.returnUrl()));
  readonly pending = signal(false);
  readonly hidePassword = signal(true);
  readonly errorMessage = signal('');
  readonly registered = signal(false);
  readonly form = this.builder.group(
    {
      userName: ['', [nonBlank]],
      email: ['', [nonBlank, Validators.email]],
      password: [
        '',
        [
          nonBlank,
          Validators.minLength(6),
          Validators.pattern(/^(?=.*[A-Z])(?=.*\d).*$/),
        ],
      ],
      confirmPassword: ['', [nonBlank]],
    },
    { validators: matchingPasswords },
  );

  submit(): void {
    if (this.pending() || this.registered()) return;
    this.form.controls.email.setValue(this.form.controls.email.value.trim());
    this.form.controls.userName.setValue(
      this.form.controls.userName.value.trim(),
    );
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
      .register({
        userName: value.userName,
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
          this.registered.set(true);
        },
        error: (error: unknown) => {
          this.form.enable();
          this.errorMessage.set(applyAuthError(error, this.form, 'register'));
        },
      });
  }
}
