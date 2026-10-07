import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import {
  EventsService,
  EventResponse,
  GroupsService,
  GroupResponse,
} from '@wishmate/api-client';
import {
  catchError,
  EMPTY,
  map,
  of,
  startWith,
  Subject,
  switchMap,
  tap,
  combineLatest,
} from 'rxjs';
import { NavigationCard } from '../../../shared/ui/navigation-card/navigation-card';

type DashboardGroup = GroupResponse & { id: number; name: string };
type DashboardEvent = EventResponse & { id: number; name: string };

@Component({
  selector: 'app-dashboard-page',
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
  private readonly groupsApi = inject(GroupsService);
  private readonly eventsApi = inject(EventsService);
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
        'Tu espacio de regalos reservados. Listado pendiente de implementación.',
      destination: '/reservations',
      action: 'Ir a reservas',
      query: null,
    },
  ];
  readonly statusLabels = {
    DRAFT: 'Borrador',
    ACTIVE: 'Activo',
    CLOSED: 'Cerrado',
    ARCHIVED: 'Archivado',
  };

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
          this.groupsApi.getMyGroups().pipe(
            tap((groups) => {
              if (
                !groups.every(
                  (group): group is DashboardGroup =>
                    Number.isSafeInteger(group.id) &&
                    (group.id ?? 0) > 0 &&
                    typeof group.name === 'string' &&
                    !!group.name.trim(),
                )
              )
                throw new Error('Invalid groups response');
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
            : this.eventsApi.getEvents({ groupId }).pipe(
                map((events) => {
                  if (
                    !events.every(
                      (event): event is DashboardEvent =>
                        Number.isSafeInteger(event.id) &&
                        (event.id ?? 0) > 0 &&
                        event.groupId === groupId &&
                        typeof event.name === 'string' &&
                        !!event.name.trim(),
                    )
                  )
                    throw new Error('Invalid events response');
                  return events;
                }),
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
}
