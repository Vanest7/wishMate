import { EventTransitionDialog } from '../ui/event-transition-dialog';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  Signal,
  signal,
} from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import {
  ActivatedRoute,
  ROUTER_OUTLET_DATA,
  RouterLink,
} from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { EventsService } from '@wishmate/api-client';
import {
  catchError,
  combineLatest,
  EMPTY,
  startWith,
  Subject,
  switchMap,
  tap,
} from 'rxjs';
import {
  canEventAction,
  EventAction,
  EventGroupContext,
  eventError,
  formatEventDate,
  ValidEvent,
  validEvent,
} from '../event-utils';
import { eventsContent } from '../events.content';
import { EventDialog, EventDialogData } from '../ui/event-dialog';

@Component({
  selector: 'app-event-detail-page',
  imports: [RouterLink, MatButtonModule, MatProgressBarModule],
  templateUrl: './event-detail-page.html',
  styleUrl: './events-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EventDetailPage {
  readonly context = inject<Signal<EventGroupContext>>(ROUTER_OUTLET_DATA);
  readonly groupId = computed(() => this.context().group.id);
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(EventsService);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);
  private readonly reload = new Subject<void>();
  readonly content = eventsContent;
  readonly formatDate = formatEventDate;
  readonly canAction = canEventAction;
  readonly event = signal<ValidEvent | null>(null);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly success = signal('');
  readonly dialogOpen = signal(false);

  constructor() {
    combineLatest([
      toObservable(this.groupId),
      this.route.paramMap,
      this.reload.pipe(startWith(undefined)),
    ])
      .pipe(
        tap(() => {
          this.event.set(null);
          this.loading.set(true);
          this.error.set('');
          this.success.set('');
        }),
        switchMap(([groupId, params]) => {
          const eventId = Number(params.get('eventId'));
          if (!Number.isSafeInteger(eventId) || eventId <= 0) {
            this.error.set(this.content.errors.missing);
            this.loading.set(false);
            return EMPTY;
          }
          return this.api.getEvent({ groupId, eventId }).pipe(
            tap((event) => {
              if (!validEvent(event, groupId) || event.id !== eventId)
                throw new Error('Invalid event response');
              this.event.set(event);
              this.loading.set(false);
            }),
            catchError((error: unknown) => {
              this.error.set(eventError(error));
              this.loading.set(false);
              return EMPTY;
            }),
          );
        }),
        takeUntilDestroyed(),
      )
      .subscribe();
  }

  retry(): void {
    this.reload.next();
  }

  act(mode: EventAction): void {
    const current = this.event();
    if (!current || this.dialogOpen() || !canEventAction(current.status, mode))
      return;
    this.dialogOpen.set(true);
    const ref = this.dialog.open<
      EventDialog | EventTransitionDialog,
      EventDialogData,
      ValidEvent
    >(mode === 'edit' ? EventDialog : EventTransitionDialog, {
      data: { mode, groupId: current.groupId, event: current },
      width: '620px',
      maxWidth: 'calc(100vw - 32px)',
      autoFocus: mode === 'edit' ? 'first-tabbable' : 'button',
    });
    const unregister = this.destroyRef.onDestroy(() => ref.close());
    ref
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((event) => {
        unregister();
        this.dialogOpen.set(false);
        if (
          event &&
          this.event()?.id === current.id &&
          this.groupId() === current.groupId &&
          validEvent(event, current.groupId)
        ) {
          this.event.set(event);
          const messages = {
            edit: this.content.updated,
            activate: this.content.activated,
            close: this.content.closed,
            archive: this.content.archived,
          };
          this.success.set(messages[mode]);
        }
      });
  }
}
