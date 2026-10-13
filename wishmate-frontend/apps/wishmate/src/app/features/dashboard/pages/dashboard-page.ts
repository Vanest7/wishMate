import {
  DashboardDataService,
  DashboardEvent,
  DashboardGroup,
} from '../data-access/dashboard-data.service';
import { eventStatusLabels } from '../../../shared/domain/event-status';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  ElementRef,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelect, MatSelectModule } from '@angular/material/select';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import {
  catchError,
  EMPTY,
  of,
  startWith,
  Subject,
  switchMap,
  tap,
  combineLatest,
} from 'rxjs';
import { NavigationCard } from '../../../shared/ui/navigation-card/navigation-card';
import { DOCUMENT } from '@angular/common';

@Component({
  selector: 'app-dashboard-page',
  providers: [DashboardDataService],
  imports: [
    NavigationCard,
    RouterLink,
    MatButtonModule,
    MatFormFieldModule,
    MatSelectModule,
    MatProgressBarModule,
  ],
  templateUrl: './dashboard-page.html',
  styleUrl: './dashboard-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardPage {
  private readonly data = inject(DashboardDataService);
  private readonly route = inject(ActivatedRoute);
  private readonly groupReload = new Subject<void>();
  private readonly eventReload = new Subject<void>();
  readonly groups = signal<DashboardGroup[]>([]);
  readonly events = signal<DashboardEvent[]>([]);
  readonly groupId = signal<number | null>(null);
  readonly group = computed(() =>
    this.groups().find((item) => item.id === this.groupId()),
  );
  readonly section = signal<'events' | 'wishes'>('wishes');
  readonly groupsLoading = signal(true);
  readonly eventsLoading = signal(false);
  readonly groupsError = signal(false);
  readonly eventsError = signal(false);
  readonly cards = [
    {
      title: 'Grupos',
      icon: '♡',
      iconColor: '#be185d',
      iconBackground: '#fce7f3',
      description: 'Organiza tu familia y amigos y gestiona sus miembros.',
      destination: '/groups',
      action: 'Ver mis grupos',
      query: null,
    },
    {
      title: 'Eventos',
      icon: '▦',
      iconColor: '#6d28d9',
      iconBackground: '#ede9fe',
      description: 'Consulta y organiza las celebraciones de tus grupos.',
      destination: '/dashboard',
      action: 'Elegir evento',
      query: { section: 'events' },
    },
    {
      title: 'Deseos',
      icon: '☆',
      iconColor: '#a16207',
      iconBackground: '#fef3c7',
      description:
        'Accede a un evento para añadir tus deseos y consultar los de los demás.',
      destination: '/dashboard',
      action: 'Ir a los deseos',
      query: { section: 'wishes' },
    },
    {
      title: 'Reservas',
      icon: '✓',
      iconColor: '#047857',
      iconBackground: '#d1fae5',
      description:
        'Consulta los regalos que has reservado y gestiona sus reservas.',
      destination: '/reservations',
      action: 'Ir a reservas',
      query: null,
    },
  ];
  readonly statusLabels = eventStatusLabels;
  private readonly document = inject(DOCUMENT);

  private readonly quickAccessSection =
    viewChild.required<ElementRef<HTMLElement>>('quickAccessSection');

  private readonly quickAccessHeading =
    viewChild.required<ElementRef<HTMLHeadingElement>>('quickAccessHeading');

  private readonly groupSelect = viewChild<MatSelect>('groupSelect');
  constructor() {
    this.route.queryParamMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      this.section.set(
        params.get('section') === 'events' ? 'events' : 'wishes',
      );
    });
    this.groupReload
      .pipe(
        startWith(undefined),
        tap(() => {
          this.groupsLoading.set(true);
          this.groupsError.set(false);
          this.groups.set([]);
          this.groupId.set(null);
        }),
        switchMap(() =>
          this.data.getGroups().pipe(
            tap((groups) => {
              this.groups.set(groups);
              if (groups.length === 1) this.groupId.set(groups[0].id);
              this.groupsLoading.set(false);
            }),
            catchError(() => {
              this.groupsError.set(true);
              this.groupsLoading.set(false);
              return EMPTY;
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe();
    combineLatest([
      toObservable(this.groupId),
      this.eventReload.pipe(startWith(undefined)),
    ])
      .pipe(
        tap(() => {
          this.events.set([]);
          this.eventsError.set(false);
          this.eventsLoading.set(this.groupId() !== null);
        }),
        switchMap(([groupId]) =>
          groupId === null
            ? of([])
            : this.data.getEvents(groupId).pipe(
                catchError(() => {
                  this.eventsError.set(true);
                  return of([]);
                }),
              ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((events) => {
        this.events.set(events);
        this.eventsLoading.set(false);
      });
  }
  selectGroup(groupId: number): void {
    this.events.set([]);
    this.eventsError.set(false);
    this.eventsLoading.set(true);
    this.groupId.set(groupId);
  }
  retryGroups(): void {
    this.groupReload.next();
  }
  retryEvents(): void {
    this.eventReload.next();
  }
  goToQuickAccess(section: string | undefined, event: MouseEvent): void {
    if (section !== 'events' && section !== 'wishes') {
      return;
    }
    if (
      event.button !== 0 ||
      event.ctrlKey ||
      event.metaKey ||
      event.shiftKey ||
      event.altKey
    ) {
      return;
    }
    this.section.set(section);
    const prefersReducedMotion =
      this.document.defaultView?.matchMedia?.(
        '(prefers-reduced-motion: reduce)',
      )?.matches ?? false;

    const behavior = prefersReducedMotion ? 'instant' : 'smooth';

    const select = this.groupSelect();

    if (select) {
      select.focus({ preventScroll: true });

      select._elementRef.nativeElement.scrollIntoView({
        behavior,
        block: 'center',
        inline: 'nearest',
      });
      return;
    }

    this.quickAccessHeading().nativeElement.focus({
      preventScroll: true,
    });

    this.quickAccessSection().nativeElement.scrollIntoView({
      behavior,
      block: 'start',
    });
  }
}
