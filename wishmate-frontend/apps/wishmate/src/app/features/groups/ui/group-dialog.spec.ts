import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { GroupsService, GroupResponse } from '@wishmate/api-client';
import { Subject } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { GroupDialog, GroupDialogData } from './group-dialog';

describe('Group mutations', () => {
  let response: Subject<GroupResponse | void>;
  let api: {
    createGroup: ReturnType<typeof vi.fn>;
    renameGroup: ReturnType<typeof vi.fn>;
    deleteGroup: ReturnType<typeof vi.fn>;
  };
  let ref: { close: ReturnType<typeof vi.fn>; disableClose: boolean };
  beforeEach(() => {
    response = new Subject<GroupResponse | void>();
    api = {
      createGroup: vi.fn(() => response),
      renameGroup: vi.fn(() => response),
      deleteGroup: vi.fn(() => response),
    };
    ref = { close: vi.fn(), disableClose: false };
  });
  function setup(data: GroupDialogData) {
    TestBed.configureTestingModule({
      providers: [
        { provide: GroupsService, useValue: api },
        { provide: MatDialogRef, useValue: ref },
        { provide: MAT_DIALOG_DATA, useValue: data },
      ],
    });
    return TestBed.createComponent(GroupDialog);
  }
  it('rejects whitespace and overlong names without sending a request', () => {
    const page = setup({ mode: 'create' }).componentInstance;
    for (const name of ['   ', 'a'.repeat(101)]) {
      page.name.setValue(name);
      page.submit();
    }
    expect(api.createGroup).not.toHaveBeenCalled();
    expect(page.name.touched).toBe(true);
  });
  it('trims names, creates once and blocks dismissal while pending', () => {
    const page = setup({ mode: 'create' }).componentInstance;
    page.name.setValue(' Familia ');
    page.submit();
    page.submit();
    page.cancel();
    expect(api.createGroup).toHaveBeenCalledExactlyOnceWith({
      body: { name: 'Familia' },
    });
    expect(ref.close).not.toHaveBeenCalled();
    expect(ref.disableClose).toBe(true);
    response.next({ id: 1, name: 'Familia' });
    response.complete();
    expect(ref.close).toHaveBeenCalledWith({
      mode: 'create',
      group: { id: 1, name: 'Familia' },
    });
    expect(page.pending()).toBe(false);
  });
  it('cancel never writes or changes the original group', () => {
    const group = { id: 1, name: 'Familia' };
    const page = setup({ mode: 'rename', group }).componentInstance;
    page.name.setValue('Otro');
    page.cancel();
    expect(group.name).toBe('Familia');
    expect(api.renameGroup).not.toHaveBeenCalled();
    expect(ref.close).toHaveBeenCalledExactlyOnceWith();
  });
  it('preserves input and maps server validation after completion', async () => {
    const fixture = setup({
      mode: 'rename',
      group: { id: 3, name: 'Familia' },
    });
    await fixture.whenStable();
    const page = fixture.componentInstance;
    page.name.setValue('Nuevo');
    page.submit();
    expect(api.renameGroup).toHaveBeenCalledWith({
      groupId: 3,
      body: { name: 'Nuevo' },
    });
    response.error(
      new HttpErrorResponse({
        status: 400,
        error: { fieldErrors: { name: 'Invalid' } },
      }),
    );
    await fixture.whenStable();
    expect(page.name.value).toBe('Nuevo');
    expect(page.name.hasError('server')).toBe(true);
    expect(page.pending()).toBe(false);
    expect(ref.disableClose).toBe(false);
    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'Revisa el nombre',
    );
    expect(ref.close).not.toHaveBeenCalled();
  });
  it('only deletes on confirmation and accepts an empty 204 response', () => {
    const page = setup({
      mode: 'delete',
      group: { id: 3, name: 'Familia' },
    }).componentInstance;
    expect(api.deleteGroup).not.toHaveBeenCalled();
    page.submit();
    page.submit();
    expect(api.deleteGroup).toHaveBeenCalledExactlyOnceWith({ groupId: 3 });
    response.next();
    response.complete();
    expect(ref.close).toHaveBeenCalledWith({ mode: 'delete' });
  });
  it.each([403, 409, 500])(
    'keeps a failed deletion open for status %s',
    (status) => {
      const page = setup({
        mode: 'delete',
        group: { id: 3, name: 'Familia' },
      }).componentInstance;
      page.submit();
      response.error(new HttpErrorResponse({ status }));
      expect(page.error()).not.toBe('');
      expect(page.pending()).toBe(false);
      expect(ref.close).not.toHaveBeenCalled();
    },
  );
  it('unsubscribes when the dialog is destroyed', () => {
    const fixture = setup({ mode: 'create' });
    fixture.componentInstance.name.setValue('Familia');
    fixture.componentInstance.submit();
    fixture.destroy();
    expect(response.observed).toBe(false);
  });
});
