import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { EventsService } from '@wishmate/api-client';
import { finalize, map } from 'rxjs';
import { EventTransitionDialogData } from '../models/event-dialog.model';
import {
  canEventAction,
  eventError,
  ValidEvent,
  validEvent,
} from '../event-utils';
import { eventsContent } from '../events.content';

@Component({
  selector: 'app-event-transition-dialog',
  imports: [MatDialogModule, MatButtonModule],
  templateUrl: './event-transition-dialog.html',
  styleUrl: './event-transition-dialog.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EventTransitionDialog {
  readonly data = inject<EventTransitionDialogData>(MAT_DIALOG_DATA);
  private readonly api = inject(EventsService);
  private readonly ref =
    inject<MatDialogRef<EventTransitionDialog, ValidEvent>>(MatDialogRef);
  private readonly destroyRef = inject(DestroyRef);
  readonly content = eventsContent;
  readonly pending = signal(false);
  readonly error = signal('');
  readonly titles = {
    activate: this.content.activateTitle,
    close: this.content.closeTitle,
    archive: this.content.archiveTitle,
  };
  readonly descriptions = {
    activate: this.content.activateDescription,
    close: this.content.closeDescription,
    archive: this.content.archiveDescription,
  };
  readonly labels = {
    activate: this.content.activate,
    close: this.content.close,
    archive: this.content.archive,
  };

  cancel(): void {
    if (!this.pending()) this.ref.close();
  }

  submit(): void {
    if (this.pending()) return;
    const { groupId, event, mode } = this.data;
    if (event.groupId !== groupId || !canEventAction(event.status, mode)) {
      this.error.set(this.content.errors.conflict);
      return;
    }
    const params = { groupId, eventId: event.id };
    const request =
      mode === 'activate'
        ? this.api.activateEvent(params)
        : mode === 'close'
          ? this.api.closeEvent(params)
          : this.api.archiveEvent(params);
    this.pending.set(true);
    this.error.set('');
    this.ref.disableClose = true;
    request
      .pipe(
        map((updated) => {
          if (!validEvent(updated, groupId) || updated.id !== event.id)
            throw new Error('Invalid event response');
          return updated;
        }),
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.pending.set(false);
          this.ref.disableClose = false;
        }),
      )
      .subscribe({
        next: (updated) => this.ref.close(updated),
        error: (error: unknown) => this.error.set(eventError(error)),
      });
  }
}
