import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  Signal,
  signal,
  TemplateRef,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ROUTER_OUTLET_DATA } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import {
  MatDialog,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { GroupMemberResponse, GroupMembersService } from '@wishmate/api-client';
import { finalize } from 'rxjs';
import { SessionService } from '../../../core/auth/session.service';
import { GroupSectionContext } from './group-section-context';
import { groupsContent } from '../groups.content';
import {
  canRemoveMember,
  canRemoveMemberWithRole,
  groupPermissions,
  groupError,
} from '../group-utils';

@Component({
  selector: 'app-group-members-page',
  imports: [MatButtonModule, MatDialogModule],
  templateUrl: './group-members-page.html',
  styleUrl: './groups-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GroupMembersPage {
  readonly context = inject<Signal<GroupSectionContext>>(ROUTER_OUTLET_DATA);
  readonly content = groupsContent;
  readonly memberCards = computed(() => {
    const { group, members } = this.context();
    const userId = this.session.userId();
    const role = groupPermissions(members, userId).role;
    return members.map((member) => ({
      member,
      canRemove: canRemoveMemberWithRole(group, role, userId, member),
    }));
  });
  readonly selectedMember = signal<GroupMemberResponse | null>(null);
  readonly pending = signal(false);
  readonly error = signal('');
  readonly success = signal('');
  private readonly api = inject(GroupMembersService);
  private readonly session = inject(SessionService);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);
  private dialogRef?: MatDialogRef<unknown>;
  private selectedGroupId?: number;
  private successTimer: ReturnType<typeof setTimeout> | undefined;

  constructor() {
    this.destroyRef.onDestroy(() => this.dialogRef?.close());
    this.destroyRef.onDestroy(() => clearTimeout(this.successTimer));
  }

  canRemove(member: GroupMemberResponse): boolean {
    const context = this.context();
    return canRemoveMember(
      context.group,
      context.members,
      this.session.userId(),
      member,
    );
  }

  openRemove(
    member: GroupMemberResponse,
    template: TemplateRef<unknown>,
  ): void {
    if (this.dialogRef || !this.canRemove(member)) return;
    this.selectedMember.set(member);
    this.selectedGroupId = this.context().group.id;
    this.error.set('');
    this.success.set('');
    const ref = this.dialog.open(template, {
      width: '440px',
      maxWidth: 'calc(100vw - 32px)',
      autoFocus: 'button',
    });
    this.dialogRef = ref;
    ref
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.dialogRef = undefined;
        this.selectedMember.set(null);
        this.selectedGroupId = undefined;
      });
  }

  cancelRemove(): void {
    if (!this.pending()) this.dialogRef?.close();
  }

  confirmRemove(): void {
    const context = this.context();
    const selected = this.selectedMember();
    const member = context.members.find(
      (item) => item.userId === selected?.userId,
    );
    const ref = this.dialogRef;
    if (
      this.pending() ||
      !ref ||
      !member ||
      member.userId === undefined ||
      context.group.id !== this.selectedGroupId ||
      !this.canRemove(member)
    )
      return;
    const groupId = context.group.id;
    const userId = member.userId;
    this.pending.set(true);
    this.error.set('');
    ref.disableClose = true;
    this.api
      .removeMember({ groupId, userId })
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.pending.set(false);
          ref.disableClose = false;
        }),
      )
      .subscribe({
        next: () => {
          context.memberRemoved(groupId, userId);
          this.showSuccess(this.content.memberRemoved);
          ref.close();
        },
        error: (error: unknown) =>
          this.error.set(
            error instanceof HttpErrorResponse && error.status === 404
              ? this.content.removeMemberMissing
              : groupError(error),
          ),
      });
  }

  private showSuccess(message: string): void {
    clearTimeout(this.successTimer);
    this.success.set(message);
    this.successTimer = setTimeout(() => this.success.set(''), 10000);
  }
}
