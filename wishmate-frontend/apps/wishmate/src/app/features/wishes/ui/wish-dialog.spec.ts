import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { WishResponse, WishesService } from '@wishmate/api-client';
import { Subject } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { SessionService } from '../../../core/auth/session.service';
import { ValidWish, WishEvent } from '../wish-utils';
import { WishDialog } from './wish-dialog';
import { WishFormDialogData as WishDialogData } from '../models/wish-dialog.model';

const event: WishEvent = {
  id: 4,
  groupId: 1,
  name: 'Birthday',
  status: 'ACTIVE',
};
const wish: ValidWish = {
  id: 5,
  eventId: 4,
  userId: 7,
  name: 'Book',
  description: 'Hardcover',
  estimatedPrice: 20,
  purchaseUrl: 'https://example.com/book',
};

describe('Wish dialog', () => {
  let response: Subject<WishResponse | void>;
  let dialogData: WishDialogData;
  let api: Record<
    'createWish' | 'updateWish' | 'deleteWish',
    ReturnType<typeof vi.fn>
  >;
  let ref: { close: ReturnType<typeof vi.fn>; disableClose: boolean };
  beforeEach(() => {
    response = new Subject();
    api = {
      createWish: vi.fn(() => response),
      updateWish: vi.fn(() => response),
      deleteWish: vi.fn(() => response),
    };
    ref = { close: vi.fn(), disableClose: false };
    TestBed.configureTestingModule({
      providers: [
        { provide: WishesService, useValue: api },
        { provide: MatDialogRef, useValue: ref },
        { provide: MAT_DIALOG_DATA, useFactory: () => dialogData },
      ],
    });
    TestBed.inject(SessionService).start(
      `a.${btoa(JSON.stringify({ sub: '7' }))}.c`,
      3600,
    );
  });
  function setup(data: WishDialogData) {
    dialogData = data;
    return TestBed.createComponent(WishDialog);
  }
  it('validates required text, lengths, price and URL before writing', () => {
    const page = setup({ mode: 'create', groupId: 1, event }).componentInstance;
    page.submit();
    page.form.patchValue({ name: ' ', description: ' ' });
    page.submit();
    page.form.patchValue({
      name: 'a'.repeat(151),
      description: 'a'.repeat(256),
    });
    page.submit();
    page.form.patchValue({
      name: 'Book',
      description: 'Hardcover',
      estimatedPrice: '1,001',
    });
    page.submit();
    page.form.patchValue({
      estimatedPrice: '20',
      purchaseUrl: 'javascript:alert(1)',
    });
    page.submit();
    expect(api.createWish).not.toHaveBeenCalled();
  });
  it('creates once with numeric price and prevents dismissal while pending', () => {
    const page = setup({ mode: 'create', groupId: 1, event }).componentInstance;
    page.form.patchValue({
      name: ' Book ',
      description: ' Hardcover ',
      estimatedPrice: '25,50',
    });
    page.submit();
    page.submit();
    page.cancel();
    expect(api.createWish).toHaveBeenCalledExactlyOnceWith({
      groupId: 1,
      eventId: 4,
      body: {
        name: 'Book',
        description: 'Hardcover',
        estimatedPrice: 25.5,
        purchaseUrl: undefined,
      },
    });
    expect(ref.disableClose).toBe(true);
    expect(ref.close).not.toHaveBeenCalled();
    response.next(wish);
    response.complete();
    expect(ref.close).toHaveBeenCalledWith({ mode: 'create', wish });
    expect(page.pending()).toBe(false);
  });
  it('allows clearing optional fields without sending owner or event in the body', () => {
    const page = setup({
      mode: 'edit',
      groupId: 1,
      event,
      wish,
    }).componentInstance;
    page.form.patchValue({ estimatedPrice: '', purchaseUrl: '' });
    page.submit();
    expect(api.updateWish).toHaveBeenCalledExactlyOnceWith({
      groupId: 1,
      eventId: 4,
      wishId: 5,
      body: {
        name: 'Book',
        description: 'Hardcover',
        estimatedPrice: undefined,
        purchaseUrl: undefined,
      },
    });
  });
  it('blocks another member from modifying a wish', () => {
    const page = setup({
      mode: 'edit',
      groupId: 1,
      event,
      wish: { ...wish, userId: 8 },
    }).componentInstance;
    page.submit();
    expect(api.updateWish).not.toHaveBeenCalled();
  });
  it.each([0, 400, 403, 404, 409, 500])(
    'retains form data after status %s',
    (status) => {
      const page = setup({
        mode: 'edit',
        groupId: 1,
        event,
        wish,
      }).componentInstance;
      page.form.controls.description.setValue('New details');
      page.submit();
      response.error(
        new HttpErrorResponse({
          status,
          error: { fieldErrors: { description: 'Invalid' } },
        }),
      );
      expect(page.form.controls.description.value).toBe('New details');
      expect(page.pending()).toBe(false);
      expect(page.form.enabled).toBe(true);
      expect(ref.disableClose).toBe(false);
      expect(ref.close).not.toHaveBeenCalled();
      if (status === 400)
        expect(page.form.controls.description.hasError('server')).toBe(true);
    },
  );
  it('rejects responses from another event or owner', () => {
    const page = setup({
      mode: 'edit',
      groupId: 1,
      event,
      wish,
    }).componentInstance;
    page.submit();
    response.next({ ...wish, userId: 8 });
    expect(ref.close).not.toHaveBeenCalled();
    expect(page.error()).not.toBe('');
  });
  it('cancel does not mutate and destruction unsubscribes', () => {
    const fixture = setup({ mode: 'edit', groupId: 1, event, wish });
    fixture.componentInstance.cancel();
    expect(api.updateWish).not.toHaveBeenCalled();
    fixture.componentInstance.submit();
    fixture.destroy();
    expect(response.observed).toBe(false);
  });
});
