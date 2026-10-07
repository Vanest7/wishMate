import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { EventsService, EventResponse } from '@wishmate/api-client';
import { Subject } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ValidEvent } from '../event-utils';
import { EventDialog } from './event-dialog';
import { EventFormDialogData as EventDialogData } from '../models/event-dialog.model';

const current: ValidEvent = {
  id: 4,
  groupId: 1,
  name: 'Birthday',
  status: 'ACTIVE',
  startDate: '2026-10-07',
  endDate: '2026-10-08',
  description: 'Party',
};

describe('Event dialog', () => {
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
  it('rejects invalid fields and date ranges before sending a request', () => {
    const page = setup({ mode: 'create', groupId: 1 }).componentInstance;
    for (const name of ['', '   ', 'a'.repeat(101)]) {
      page.form.controls.name.setValue(name);
      page.submit();
    }
    page.form.controls.name.setValue('Birthday');
    page.form.controls.description.setValue('a'.repeat(256));
    page.submit();
    page.form.controls.description.setValue('');
    page.form.patchValue({ startDate: '2026-10-08', endDate: '2026-10-07' });
    page.submit();
    expect(page.form.hasError('dateRange')).toBe(true);
    page.form.patchValue({ startDate: '2026-02-31', endDate: '' });
    page.submit();
    expect(api.createEvent).not.toHaveBeenCalled();
  });
  it('creates once, trims text and sends dates unchanged', () => {
    const page = setup({ mode: 'create', groupId: 1 }).componentInstance;
    page.form.patchValue({
      name: ' Birthday ',
      startDate: '2026-10-07',
      endDate: '2026-10-07',
      status: 'DRAFT',
    });
    page.submit();
    page.submit();
    page.cancel();
    expect(api.createEvent).toHaveBeenCalledExactlyOnceWith({
      groupId: 1,
      body: {
        name: 'Birthday',
        description: undefined,
        startDate: '2026-10-07',
        endDate: '2026-10-07',
        status: 'DRAFT',
      },
    });
    expect(ref.disableClose).toBe(true);
    expect(ref.close).not.toHaveBeenCalled();
    response.next({ ...current, status: 'DRAFT' });
    response.complete();
    expect(ref.close).toHaveBeenCalledWith({ ...current, status: 'DRAFT' });
    expect(page.pending()).toBe(false);
  });
  it('edits fields without sending status, IDs or creator in the body and permits clearing optional values', () => {
    const page = setup({
      mode: 'edit',
      groupId: 1,
      event: current,
    }).componentInstance;
    page.form.patchValue({
      name: 'Updated',
      description: '',
      startDate: '',
      endDate: '',
    });
    page.submit();
    expect(api.updateEvent).toHaveBeenCalledExactlyOnceWith({
      groupId: 1,
      eventId: 4,
      body: {
        name: 'Updated',
        description: undefined,
        startDate: undefined,
        endDate: undefined,
      },
    });
    expect(current.description).toBe('Party');
  });
  it('blocks editing archived events', () => {
    const page = setup({
      mode: 'edit',
      groupId: 1,
      event: { ...current, status: 'ARCHIVED' },
    }).componentInstance;
    page.submit();
    expect(api.updateEvent).not.toHaveBeenCalled();
  });
  it.each([0, 400, 403, 404, 409, 500])(
    'keeps the dialog open and restores input after status %s',
    (status) => {
      const page = setup({
        mode: 'edit',
        groupId: 1,
        event: current,
      }).componentInstance;
      page.form.controls.name.setValue('Changed');
      page.submit();
      response.error(
        new HttpErrorResponse({
          status,
          error: { fieldErrors: { name: 'Invalid' } },
        }),
      );
      expect(page.form.controls.name.value).toBe('Changed');
      expect(page.form.enabled).toBe(true);
      expect(page.pending()).toBe(false);
      expect(ref.disableClose).toBe(false);
      expect(page.error()).not.toBe('');
      expect(ref.close).not.toHaveBeenCalled();
      if (status === 400)
        expect(page.form.controls.name.hasError('server')).toBe(true);
    },
  );
  it('rejects responses belonging to another group', () => {
    const page = setup({ mode: 'create', groupId: 1 }).componentInstance;
    page.form.controls.name.setValue('Birthday');
    page.submit();
    response.next({ ...current, groupId: 2 });
    expect(ref.close).not.toHaveBeenCalled();
    expect(page.error()).not.toBe('');
    expect(page.pending()).toBe(false);
  });
  it('cancel does not write and destruction unsubscribes pending work', () => {
    const fixture = setup({ mode: 'create', groupId: 1 });
    fixture.componentInstance.cancel();
    expect(api.createEvent).not.toHaveBeenCalled();
    fixture.componentInstance.form.controls.name.setValue('Birthday');
    fixture.componentInstance.submit();
    fixture.destroy();
    expect(response.observed).toBe(false);
  });
});
