import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import {
  GroupResponse,
  GroupMemberResponse,
  GroupsService,
  GroupMembersService,
} from '@wishmate/api-client';
import {
  catchError,
  combineLatest,
  EMPTY,
  forkJoin,
  startWith,
  Subject,
  switchMap,
  tap,
} from 'rxjs';
import { SessionService } from '../../../core/auth/session.service';
import { groupsContent } from '../groups.content';
import { groupError, groupPermissions, validGroup } from '../group-utils';
import {
  GroupDialog,
  GroupDialogData,
  GroupDialogResult,
} from '../ui/group-dialog';

@Component({
  selector: 'app-group-detail-page',
  imports: [RouterLink, MatButtonModule, MatProgressBarModule],
  templateUrl: './group-detail-page.html',
  styleUrl: './groups-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GroupDetailPage {
  private readonly api = inject(GroupsService);
  private readonly membersApi = inject(GroupMembersService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);
  private readonly session = inject(SessionService);
  private readonly reload = new Subject<void>();
  readonly content = groupsContent;
  readonly group = signal<(GroupResponse & { id: number }) | null>(null);
  readonly members = signal<GroupMemberResponse[]>([]);
  readonly membersOnly = signal(false);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly success = signal('');
  readonly dialogOpen = signal(false);
  readonly permissions = computed(() =>
    groupPermissions(this.members(), this.session.userId()),
  );

  constructor() {
    combineLatest([
      this.route.paramMap,
      this.route.data,
      this.reload.pipe(startWith(undefined)),
    ])
      .pipe(
        tap(() => {
          this.group.set(null);
          this.members.set([]);
          this.loading.set(true);
          this.error.set('');
        }),
        switchMap(([params, data]) => {
          const groupId = Number(params.get('groupId'));
          this.membersOnly.set(data['membersOnly'] === true);
          return forkJoin({
            group: this.api.getGroup({ groupId }),
            members: this.membersApi.getMembers({ groupId }),
          }).pipe(
            tap(({ group, members }) => {
              if (!validGroup(group) || group.id !== groupId)
                throw new Error('Invalid group response');
              this.group.set(group);
              this.members.set(members);
              this.loading.set(false);
            }),
            catchError((error: unknown) => {
              this.error.set(groupError(error));
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
  edit(mode: 'rename' | 'delete'): void {
    const group = this.group();
    if (
      !group ||
      this.dialogOpen() ||
      (mode === 'rename'
        ? !this.permissions().canRename
        : !this.permissions().canDelete)
    )
      return;
    this.dialogOpen.set(true);
    const dialogRef = this.dialog.open<
      GroupDialog,
      GroupDialogData,
      GroupDialogResult
    >(GroupDialog, {
      data: { mode, group },
      width: '520px',
      maxWidth: 'calc(100vw - 32px)',
      autoFocus: mode === 'delete' ? 'button' : 'first-tabbable',
    });
    const unregister = this.destroyRef.onDestroy(() => dialogRef.close());
    dialogRef
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        unregister();
        this.dialogOpen.set(false);
        if (result?.mode === 'delete') {
          this.snackBar.open(this.content.deleted, undefined, {
            duration: 5000,
          });
          void this.router.navigate(['/groups']);
        } else if (result?.mode === 'rename') {
          this.success.set(this.content.renamed);
          this.reload.next();
        }
      });
  }
}
