import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { EventsService, EventResponse } from '@wishmate/api-client';
import { Subject } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ValidEvent } from '../event-utils';
import { EventTransitionDialog as EventDialog } from './event-transition-dialog';
import { EventTransitionDialogData as EventDialogData } from '../models/event-dialog.model';

const current: ValidEvent = {
  id: 4,
  groupId: 1,
  name: 'Birthday',
  status: 'ACTIVE',
  startDate: '2026-10-07',
  endDate: '2026-10-08',
  description: 'Party',
};

describe('Event transition dialog', () => {
  let response: Subject<EventResponse>;
  let api: Record<
    | 'createEvent'
    | 'updateEvent'
    | 'activateEvent'
    | 'closeEvent'
    | 'archiveEvent',
    ReturnType<typeof vi.fn>
  >;
  let ref: { close: ReturnType<typeof vi.fn>; disableClose: boolean };
  beforeEach(() => {
    response = new Subject();
    api = {
      createEvent: vi.fn(() => response),
      updateEvent: vi.fn(() => response),
      activateEvent: vi.fn(() => response),
      closeEvent: vi.fn(() => response),
      archiveEvent: vi.fn(() => response),
    };
    ref = { close: vi.fn(), disableClose: false };
  });
  function setup(data: EventDialogData) {
    TestBed.configureTestingModule({
      providers: [
        { provide: EventsService, useValue: api },
        { provide: MatDialogRef, useValue: ref },
        { provide: MAT_DIALOG_DATA, useValue: data },
      ],
    });
    return TestBed.createComponent(EventDialog);
  }
  it.each([
    ['activate', 'DRAFT', 'activateEvent'],
    ['close', 'ACTIVE', 'closeEvent'],
    ['archive', 'CLOSED', 'archiveEvent'],
  ] as const)(
    'confirms %s using its dedicated endpoint',
    (mode, status, method) => {
      const page = setup({
        mode,
        groupId: 1,
        event: { ...current, status },
      }).componentInstance;
      expect(api[method]).not.toHaveBeenCalled();
      page.submit();
      page.submit();
      expect(api[method]).toHaveBeenCalledExactlyOnceWith({
        groupId: 1,
        eventId: 4,
      });
      expect(api.updateEvent).not.toHaveBeenCalled();
    },
  );
  it.each(['activate', 'close', 'archive'] as const)(
    'blocks %s for archived events',
    (mode) => {
      const page = setup({
        mode,
        groupId: 1,
        event: { ...current, status: 'ARCHIVED' },
      }).componentInstance;
      page.submit();
      for (const request of Object.values(api))
        expect(request).not.toHaveBeenCalled();
    },
  );
  it('preserves the confirmation after a conflict', () => {
    const page = setup({
      mode: 'close',
      groupId: 1,
      event: current,
    }).componentInstance;
    page.submit();
    response.error(new HttpErrorResponse({ status: 409 }));
    expect(page.pending()).toBe(false);
    expect(ref.disableClose).toBe(false);
    expect(ref.close).not.toHaveBeenCalled();
    expect(page.error()).not.toBe('');
  });
  it('rejects a response from another event', () => {
    const page = setup({
      mode: 'close',
      groupId: 1,
      event: current,
    }).componentInstance;
    page.submit();
    response.next({ ...current, id: 8, status: 'CLOSED' });
    expect(ref.close).not.toHaveBeenCalled();
    expect(page.error()).not.toBe('');
  });
});
