import { HttpErrorResponse } from '@angular/common/http';
import {
  AbstractControl,
  FormGroup,
  ValidationErrors,
  ValidatorFn,
} from '@angular/forms';
import { authContent } from './auth.content';

export const nonBlank: ValidatorFn = (
  control: AbstractControl,
): ValidationErrors | null =>
  typeof control.value === 'string' && control.value.trim().length > 0
    ? null
    : { required: true };

export const matchingPasswords: ValidatorFn = (
  control: AbstractControl,
): ValidationErrors | null =>
  control.get('password')?.value === control.get('confirmPassword')?.value
    ? null
    : { mismatch: true };

export function applyAuthError(
  error: unknown,
  form: FormGroup,
  mode: 'login' | 'register',
): string {
  const messages = authContent.errors;
  if (!(error instanceof HttpErrorResponse)) return messages.generic;
  if (error.status === 0) return messages.network;
  if (error.status === 401 && mode === 'login')
    return messages.invalidCredentials;
  if (error.status === 403) return messages.forbidden;
  if (error.status === 409 && mode === 'register') return messages.conflict;
  if (error.status === 400) {
    const body: unknown = error.error;
    if (body && typeof body === 'object' && 'fieldErrors' in body) {
      const fields = body.fieldErrors;
      if (fields && typeof fields === 'object') {
        for (const key of ['userName', 'email', 'password']) {
          if (
            Object.entries(fields).some(
              ([name, message]) => name === key && typeof message === 'string',
            )
          ) {
            const control = form.get(key);
            control?.setErrors({ ...control.errors, server: true });
            control?.markAsTouched();
          }
        }
      }
    }
    return messages.validation;
  }
  return messages.generic;
}
