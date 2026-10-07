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
import { ROUTER_OUTLET_DATA, RouterLink } from '@angular/router';
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
  EventGroupContext,
  eventError,
  formatEventDate,
  ValidEvent,
  validEvent,
} from '../event-utils';
import { eventsContent } from '../events.content';
import { EventDialog, EventDialogData } from '../ui/event-dialog';

@Component({
  selector: 'app-event-list-page',
  imports: [RouterLink, MatButtonModule, MatProgressBarModule],
  templateUrl: './event-list-page.html',
  styleUrl: './events-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EventListPage {
  readonly context = inject<Signal<EventGroupContext>>(ROUTER_OUTLET_DATA);
  readonly groupId = computed(() => this.context().group.id);
  private readonly api = inject(EventsService);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);
  private readonly reload = new Subject<void>();
  readonly content = eventsContent;
  readonly formatDate = formatEventDate;
  readonly events = signal<ValidEvent[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly success = signal('');
  readonly dialogOpen = signal(false);

  constructor() {
    combineLatest([
      toObservable(this.groupId),
      this.reload.pipe(startWith(undefined)),
    ])
      .pipe(
        tap(() => {
          this.events.set([]);
          this.loading.set(true);
          this.error.set('');
        }),
        switchMap(([groupId]) =>
          this.api.getEvents({ groupId }).pipe(
            tap((events) => {
              if (
                !events.every((event): event is ValidEvent =>
                  validEvent(event, groupId),
                ) ||
                new Set(events.map((event) => event.id)).size !== events.length
              )
                throw new Error('Invalid events response');
              this.events.set(events);
              this.loading.set(false);
            }),
            catchError((error: unknown) => {
              this.error.set(eventError(error));
              this.loading.set(false);
              return EMPTY;
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe();
  }

  retry(): void {
    this.reload.next();
  }

  create(): void {
    if (this.dialogOpen() || this.loading() || this.error()) return;
    const groupId = this.groupId();
    this.dialogOpen.set(true);
    const ref = this.dialog.open<EventDialog, EventDialogData, ValidEvent>(
      EventDialog,
      {
        data: { mode: 'create', groupId },
        width: '620px',
        maxWidth: 'calc(100vw - 32px)',
      },
    );
    const unregister = this.destroyRef.onDestroy(() => ref.close());
    ref
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((event) => {
        unregister();
        this.dialogOpen.set(false);
        if (event && this.groupId() === groupId && validEvent(event, groupId)) {
          this.success.set(this.content.created);
          this.reload.next();
        }
      });
  }
}
