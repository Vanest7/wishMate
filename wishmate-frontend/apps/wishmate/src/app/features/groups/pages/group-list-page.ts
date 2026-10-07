import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { GroupResponse, GroupsService } from '@wishmate/api-client';
import {
  catchError,
  EMPTY,
  map,
  startWith,
  Subject,
  switchMap,
  tap,
} from 'rxjs';
import { groupsContent } from '../groups.content';
import { groupError, validGroup } from '../group-utils';
import {
  GroupDialog,
  GroupDialogData,
  GroupDialogResult,
} from '../ui/group-dialog';

@Component({
  selector: 'app-group-list-page',
  imports: [RouterLink, MatButtonModule, MatProgressBarModule],
  templateUrl: './group-list-page.html',
  styleUrl: './groups-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GroupListPage {
  private readonly api = inject(GroupsService);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);
  private readonly reload = new Subject<void>();
  readonly content = groupsContent;
  readonly groups = signal<(GroupResponse & { id: number })[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly success = signal('');
  readonly dialogOpen = signal(false);

  constructor() {
    this.reload
      .pipe(
        startWith(undefined),
        tap(() => {
          this.loading.set(true);
          this.error.set('');
        }),
        switchMap(() =>
          this.api.getMyGroups().pipe(
            map((groups) => {
              if (!groups.every(validGroup))
                throw new Error('Invalid groups response');
              return groups.filter(validGroup);
            }),
            tap((groups) => {
              this.groups.set(groups);
              this.loading.set(false);
            }),
            catchError((error: unknown) => {
              this.error.set(groupError(error));
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
    if (this.dialogOpen()) return;
    this.dialogOpen.set(true);
    const dialogRef = this.dialog.open<
      GroupDialog,
      GroupDialogData,
      GroupDialogResult
    >(GroupDialog, {
      data: { mode: 'create' },
      width: '520px',
      maxWidth: 'calc(100vw - 32px)',
    });
    const unregister = this.destroyRef.onDestroy(() => dialogRef.close());
    dialogRef
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        unregister();
        this.dialogOpen.set(false);
        if (result?.mode === 'create') {
          this.success.set(this.content.created);
          this.reload.next();
        }
      });
  }
}
