import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthService } from '../data-access/auth.service';
import { LoginPage } from './login-page';
import { RegisterPage } from './register-page';

describe('Authentication forms', () => {
  let result: Subject<void>;
  let auth: {
    login: ReturnType<typeof vi.fn>;
    register: ReturnType<typeof vi.fn>;
  };
  beforeEach(() => {
    result = new Subject<void>();
    auth = { login: vi.fn(() => result), register: vi.fn(() => result) };
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: AuthService, useValue: auth }],
    });
  });

  it('blocks blank login and displays required feedback after submit', async () => {
    const fixture = TestBed.createComponent(LoginPage);
    fixture.componentInstance.submit();
    await fixture.whenStable();
    expect(auth.login).not.toHaveBeenCalled();
    expect(fixture.componentInstance.form.controls.email.touched).toBe(true);
    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'Completa este campo.',
    );
  });

  it('normalizes email, preserves password and blocks double submission', () => {
    const page = TestBed.createComponent(LoginPage).componentInstance;
    page.form.setValue({ email: ' test@example.com ', password: ' secret ' });
    page.submit();
    page.submit();
    expect(auth.login).toHaveBeenCalledExactlyOnceWith({
      email: 'test@example.com',
      password: ' secret ',
    });
    expect(page.pending()).toBe(true);
    result.error(new HttpErrorResponse({ status: 401 }));
    expect(page.pending()).toBe(false);
    expect(page.form.enabled).toBe(true);
    expect(page.form.controls.password.value).toBe(' secret ');
    expect(page.errorMessage()).toContain('no son correctos');
  });

  it('returns to the private deep link on successful login', () => {
    const fixture = TestBed.createComponent(LoginPage);
    fixture.componentRef.setInput('returnUrl', '/groups/12/events/34/wishes');
    const navigate = vi
      .spyOn(TestBed.inject(Router), 'navigateByUrl')
      .mockResolvedValue(true);
    fixture.componentInstance.form.setValue({
      email: 'test@example.com',
      password: 'secret',
    });
    fixture.componentInstance.submit();
    result.next();
    result.complete();
    expect(navigate).toHaveBeenCalledWith('/groups/12/events/34/wishes', {
      replaceUrl: true,
    });
    expect(fixture.componentInstance.form.controls.password.value).toBe('');
  });

  it('shows no invalid fields after a successful native form submission while navigation is pending', async () => {
    const fixture = TestBed.createComponent(LoginPage);
    const navigate = vi
      .spyOn(TestBed.inject(Router), 'navigateByUrl')
      .mockImplementation(() => new Promise<boolean>(() => undefined));
    await fixture.whenStable();
    fixture.componentInstance.form.setValue({
      email: 'test@example.com',
      password: 'secret',
    });
    fixture.nativeElement
      .querySelector('form')
      .dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    result.next();
    result.complete();
    await fixture.whenStable();
    expect(navigate).toHaveBeenCalledWith('/dashboard', { replaceUrl: true });
    expect(
      fixture.nativeElement.querySelector('.mat-form-field-invalid'),
    ).toBeNull();
    expect(fixture.nativeElement.textContent).toContain('Sesión iniciada');
    expect(fixture.componentInstance.form.controls.password.value).toBe('');
    fixture.componentInstance.submit();
    expect(auth.login).toHaveBeenCalledTimes(1);
  });

  it('blocks mismatching passwords and whitespace-only names', () => {
    const page = TestBed.createComponent(RegisterPage).componentInstance;
    page.form.setValue({
      userName: ' ',
      handle: 'test_user',
      email: 'test@example.com',
      password: 'Secret1',
      confirmPassword: 'Other1',
    });
    page.submit();
    expect(auth.register).not.toHaveBeenCalled();
    expect(page.form.hasError('mismatch')).toBe(true);
    expect(page.form.controls.userName.hasError('required')).toBe(true);
  });

  it('enforces password length, uppercase, and number requirements', () => {
    const page = TestBed.createComponent(RegisterPage).componentInstance;
    const password = page.form.controls.password;

    password.setValue('short');
    expect(password.hasError('minlength')).toBe(true);
    expect(password.hasError('pattern')).toBe(true);

    password.setValue('longpassword');
    expect(password.hasError('minlength')).toBe(false);
    expect(password.hasError('pattern')).toBe(true);

    password.setValue('Longpass1');
    expect(password.valid).toBe(true);
  });

  it('sends only the registration DTO and shows success without automatic login', () => {
    const page = TestBed.createComponent(RegisterPage).componentInstance;
    page.form.setValue({
      userName: ' Test ',
      handle: 'test_user',
      email: 'test@example.com',
      password: ' Secret1 ',
      confirmPassword: ' Secret1 ',
    });
    page.submit();
    page.submit();
    expect(auth.register).toHaveBeenCalledExactlyOnceWith({
      userName: 'Test',
      handle: 'test_user',
      email: 'test@example.com',
      password: ' Secret1 ',
    });
    result.next();
    result.complete();
    expect(page.registered()).toBe(true);
    expect(page.form.controls.password.value).toBe('');
    expect(auth.login).not.toHaveBeenCalled();
  });

  it('retains server field errors after the request finalizes', () => {
    const page = TestBed.createComponent(RegisterPage).componentInstance;
    page.form.setValue({
      userName: 'Test',
      handle: 'test_user',
      email: 'test@example.com',
      password: 'Secret1',
      confirmPassword: 'Secret1',
    });
    page.submit();
    result.error(
      new HttpErrorResponse({
        status: 400,
        error: { fieldErrors: { email: 'Invalid email' } },
      }),
    );
    expect(page.form.controls.email.hasError('server')).toBe(true);
    expect(page.form.controls.email.value).toBe('test@example.com');
    expect(page.pending()).toBe(false);
  });

  it('cleans up a pending request when the page is destroyed', () => {
    const fixture = TestBed.createComponent(LoginPage);
    fixture.componentInstance.form.setValue({
      email: 'test@example.com',
      password: 'secret',
    });
    fixture.componentInstance.submit();
    fixture.destroy();
    expect(result.observed).toBe(false);
  });
});
