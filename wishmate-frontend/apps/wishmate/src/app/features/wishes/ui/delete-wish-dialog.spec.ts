import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { WishResponse, WishesService } from '@wishmate/api-client';
import { Subject } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { SessionService } from '../../../core/auth/session.service';
import { ValidWish, WishEvent } from '../wish-utils';
import { DeleteWishDialog as WishDialog } from './delete-wish-dialog';
import { DeleteWishDialogData as WishDialogData } from '../models/wish-dialog.model';

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

describe('Delete wish dialog', () => {
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
  it('deletes only after confirmation and accepts a 204 response', () => {
    const page = setup({
      mode: 'delete',
      groupId: 1,
      event,
      wish,
    }).componentInstance;
    expect(api.deleteWish).not.toHaveBeenCalled();
    page.submit();
    page.submit();
    expect(api.deleteWish).toHaveBeenCalledExactlyOnceWith({
      groupId: 1,
      eventId: 4,
      wishId: 5,
    });
    response.next();
    response.complete();
    expect(ref.close).toHaveBeenCalledWith({ mode: 'delete', wishId: 5 });
  });
  it.each(['DRAFT', 'CLOSED', 'ARCHIVED'] as const)(
    'blocks mutations in %s',
    (status) => {
      const page = setup({
        mode: 'delete',
        groupId: 1,
        event: { ...event, status },
        wish,
      }).componentInstance;
      page.submit();
      expect(api.deleteWish).not.toHaveBeenCalled();
    },
  );
  it('preserves confirmation and allows retry after failure', () => {
    const page = setup({
      mode: 'delete',
      groupId: 1,
      event,
      wish,
    }).componentInstance;
    page.submit();
    response.error(new HttpErrorResponse({ status: 409 }));
    expect(page.pending()).toBe(false);
    expect(ref.disableClose).toBe(false);
    expect(ref.close).not.toHaveBeenCalled();
    expect(page.error()).not.toBe('');
  });
  it('blocks deleting another user wish', () => {
    const page = setup({
      mode: 'delete',
      groupId: 1,
      event,
      wish: { ...wish, userId: 8 },
    }).componentInstance;
    page.submit();
    expect(api.deleteWish).not.toHaveBeenCalled();
  });
});
