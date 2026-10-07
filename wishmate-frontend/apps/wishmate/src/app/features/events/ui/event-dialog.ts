import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import {
  EventResponse,
  EventsService,
  UpdateEventRequest,
} from '@wishmate/api-client';
import { finalize, map, Observable } from 'rxjs';
import {
  canEventAction,
  EventAction,
  eventError,
  ValidEvent,
  validDateOnly,
  validEvent,
} from '../event-utils';
import { eventsContent } from '../events.content';

export type EventDialogData =
  | { mode: 'create'; groupId: number }
  | { mode: EventAction; groupId: number; event: ValidEvent };

@Component({
  selector: 'app-event-dialog',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
  ],
  templateUrl: './event-dialog.html',
  styleUrl: './event-dialog.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EventDialog {
  readonly data = inject<EventDialogData>(MAT_DIALOG_DATA);
  private readonly ref =
    inject<MatDialogRef<EventDialog, ValidEvent>>(MatDialogRef);
  private readonly api = inject(EventsService);
  private readonly destroyRef = inject(DestroyRef);
  readonly content = eventsContent;
  readonly pending = signal(false);
  readonly error = signal('');
  readonly isForm = this.data.mode === 'create' || this.data.mode === 'edit';
  private readonly initial =
    this.data.mode === 'create' ? undefined : this.data.event;
  readonly form = new FormGroup(
    {
      name: new FormControl(this.initial?.name ?? '', {
        nonNullable: true,
        validators: [
          (control) =>
            typeof control.value === 'string' && control.value.trim()
              ? null
              : { required: true },
          Validators.maxLength(100),
        ],
      }),
      description: new FormControl(this.initial?.description ?? '', {
        nonNullable: true,
        validators: [Validators.maxLength(255)],
      }),
      startDate: new FormControl(this.initial?.startDate ?? '', {
        nonNullable: true,
        validators: [
          (control) =>
            typeof control.value === 'string' && validDateOnly(control.value)
              ? null
              : { date: true },
        ],
      }),
      endDate: new FormControl(this.initial?.endDate ?? '', {
        nonNullable: true,
        validators: [
          (control) =>
            typeof control.value === 'string' && validDateOnly(control.value)
              ? null
              : { date: true },
        ],
      }),
      status: new FormControl<'DRAFT' | 'ACTIVE'>('ACTIVE', {
        nonNullable: true,
        validators: [
          (control) =>
            control.value === 'DRAFT' || control.value === 'ACTIVE'
              ? null
              : { status: true },
        ],
      }),
    },
    {
      validators: [
        (control) => {
          const start: unknown = control.get('startDate')?.value;
          const end: unknown = control.get('endDate')?.value;
          return typeof start === 'string' &&
            typeof end === 'string' &&
            start &&
            end &&
            validDateOnly(start) &&
            validDateOnly(end) &&
            end < start
            ? { dateRange: true }
            : null;
        },
      ],
    },
  );
  readonly titles = {
    create: this.content.createTitle,
    edit: this.content.editTitle,
    activate: this.content.activateTitle,
    close: this.content.closeTitle,
    archive: this.content.archiveTitle,
  };
  readonly descriptions = {
    create: this.content.createDescription,
    edit: this.content.editDescription,
    activate: this.content.activateDescription,
    close: this.content.closeDescription,
    archive: this.content.archiveDescription,
  };
  readonly labels = {
    create: this.content.create,
    edit: this.content.save,
    activate: this.content.activate,
    close: this.content.close,
    archive: this.content.archive,
  };

  cancel(): void {
    if (!this.pending()) this.ref.close();
  }

  submit(): void {
    if (this.pending()) return;
    if (this.isForm && this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    if (
      this.data.mode !== 'create' &&
      (this.data.event.groupId !== this.data.groupId ||
        !canEventAction(this.data.event.status, this.data.mode))
    ) {
      this.error.set(this.content.errors.conflict);
      return;
    }
    this.pending.set(true);
    this.error.set('');
    const request = this.request();
    this.form.disable();
    this.ref.disableClose = true;
    request
      .pipe(
        map((event) => {
          if (
            !validEvent(event, this.data.groupId) ||
            (this.data.mode !== 'create' && event.id !== this.data.event.id)
          )
            throw new Error('Invalid event response');
          return event;
        }),
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.pending.set(false);
          this.ref.disableClose = false;
          if (this.form.disabled) this.form.enable();
        }),
      )
      .subscribe({
        next: (event) => this.ref.close(event),
        error: (error: unknown) => {
          this.form.enable();
          this.error.set(eventError(error));
          if (error instanceof HttpErrorResponse && error.status === 400) {
            const body: unknown = error.error;
            if (
              body &&
              typeof body === 'object' &&
              'fieldErrors' in body &&
              body.fieldErrors &&
              typeof body.fieldErrors === 'object'
            ) {
              const fields = body.fieldErrors;
              for (const key of [
                'name',
                'description',
                'startDate',
                'endDate',
                'status',
              ] as const) {
                if (key in fields) {
                  this.form.controls[key].setErrors({
                    ...this.form.controls[key].errors,
                    server: true,
                  });
                  this.form.controls[key].markAsTouched();
                }
              }
            }
          }
        },
      });
  }

  private request(): Observable<EventResponse> {
    const data = this.data;
    const raw = this.form.getRawValue();
    const body: UpdateEventRequest = {
      name: raw.name.trim(),
      description: raw.description.trim() || undefined,
      startDate: raw.startDate || undefined,
      endDate: raw.endDate || undefined,
    };
    if (data.mode === 'create')
      return this.api.createEvent({
        groupId: data.groupId,
        body: { ...body, status: raw.status },
      });
    const params = { groupId: data.groupId, eventId: data.event.id };
    switch (data.mode) {
      case 'edit':
        return this.api.updateEvent({ ...params, body });
      case 'activate':
        return this.api.activateEvent(params);
      case 'close':
        return this.api.closeEvent(params);
      case 'archive':
        return this.api.archiveEvent(params);
    }
  }
}
