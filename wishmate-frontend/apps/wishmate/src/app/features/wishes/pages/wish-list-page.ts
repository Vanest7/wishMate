import { DeleteWishDialog } from '../ui/delete-wish-dialog';
import { WishCard } from '../ui/wish-card';
import { WishCardModel } from '../models/wish-card.model';
import { WishesDataService } from '../data-access/wishes-data.service';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import {
  catchError,
  combineLatest,
  EMPTY,
  startWith,
  Subject,
  switchMap,
  tap,
} from 'rxjs';
import { SessionService } from '../../../core/auth/session.service';
import {
  canManageWish,
  formatPrice,
  positiveId,
  safePurchaseUrl,
  ValidWish,
  validWish,
  wishError,
  WishPageData,
} from '../wish-utils';
import { wishesContent } from '../wishes.content';
import {
  WishDialog,
  WishDialogData,
  WishDialogResult,
} from '../ui/wish-dialog';

@Component({
  selector: 'app-wish-list-page',
  providers: [WishesDataService],
  imports: [MatButtonModule, RouterLink, MatProgressBarModule, WishCard],
  templateUrl: './wish-list-page.html',
  styleUrl: './wish-list-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WishListPage {
  private readonly wishesData = inject(WishesDataService);
  private readonly route = inject(ActivatedRoute);
  private readonly session = inject(SessionService);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);
  private readonly reload = new Subject<void>();
  private dialogRef?: MatDialogRef<
    WishDialog | DeleteWishDialog,
    WishDialogResult
  >;
  readonly content = wishesContent;
  readonly data = signal<WishPageData | null>(null);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly success = signal('');
  readonly dialogOpen = signal(false);
  readonly mineOnly = signal(false);
  readonly canCreate = computed(
    () =>
      this.data()?.event.status === 'ACTIVE' &&
      positiveId(this.session.userId()),
  );
  private readonly allCards = computed<WishCardModel[]>(() => {
    const data = this.data();
    const userId = this.session.userId();
    if (!data) return [];
    const names = new Map(
      data.members.map((member) => [member.userId, member.userName]),
    );
    return data.wishes.map((wish) => ({
      wish,
      ownerName: names.get(wish.userId) || this.content.unknownOwner,
      isMine: wish.userId === userId,
      canManage: canManageWish(wish, data.event, userId),
      purchaseHref: safePurchaseUrl(wish.purchaseUrl),
      price: formatPrice(wish.estimatedPrice),
    }));
  });

  readonly cards = computed(() =>
    this.mineOnly()
      ? this.allCards().filter((card) => card.isMine)
      : this.allCards(),
  );

  constructor() {
    this.destroyRef.onDestroy(() => this.dialogRef?.close());
    combineLatest([
      this.route.paramMap,
      this.reload.pipe(startWith(undefined)),
      toObservable(this.session.userId),
    ])
      .pipe(
        tap(() => {
          this.dialogRef?.close();
          this.data.set(null);
          this.loading.set(true);
          this.error.set('');
          this.success.set('');
        }),
        switchMap(([params, , userId]) => {
          const rawGroupId = params.get('groupId') ?? '';
          const rawEventId = params.get('eventId') ?? '';
          const groupId = Number(rawGroupId),
            eventId = Number(rawEventId);
          if (!positiveId(userId)) {
            this.loading.set(false);
            this.error.set(this.content.errors.forbidden);
            return EMPTY;
          }
          if (
            !/^[1-9]\d*$/.test(rawGroupId) ||
            !/^[1-9]\d*$/.test(rawEventId) ||
            !positiveId(groupId) ||
            !positiveId(eventId)
          ) {
            this.loading.set(false);
            this.error.set(this.content.errors.missing);
            return EMPTY;
          }
          return this.wishesData.getPage(groupId, eventId).pipe(
            tap((data) => {
              this.data.set(data);
              this.loading.set(false);
            }),
            catchError((error: unknown) => {
              this.error.set(wishError(error));
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
    if (!this.dialogOpen()) this.reload.next();
  }

  create(): void {
    const data = this.data();
    if (!data || !this.canCreate() || this.dialogOpen()) return;
    this.open({ mode: 'create', groupId: data.group.id, event: data.event });
  }

  edit(mode: 'edit' | 'delete', wish: ValidWish): void {
    const data = this.data();
    const current = data?.wishes.find((item) => item.id === wish.id);
    if (
      !data ||
      !current ||
      this.dialogOpen() ||
      !canManageWish(current, data.event, this.session.userId())
    )
      return;
    this.open({
      mode,
      groupId: data.group.id,
      event: data.event,
      wish: current,
    });
  }

  private open(data: WishDialogData): void {
    const userId = this.session.userId();
    this.dialogOpen.set(true);
    this.success.set('');
    const ref = this.dialog.open<
      WishDialog | DeleteWishDialog,
      WishDialogData,
      WishDialogResult
    >(data.mode === 'delete' ? DeleteWishDialog : WishDialog, {
      data,
      width: '620px',
      maxWidth: 'calc(100vw - 32px)',
      autoFocus: data.mode === 'delete' ? 'button' : 'first-tabbable',
    });
    this.dialogRef = ref;
    ref
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        if (this.dialogRef === ref) {
          this.dialogRef = undefined;
          this.dialogOpen.set(false);
        }
        const current = this.data();
        if (
          !result ||
          !current ||
          current.group.id !== data.groupId ||
          current.event.id !== data.event.id ||
          this.session.userId() !== userId
        )
          return;
        if (result.mode === 'delete') {
          this.data.set({
            ...current,
            wishes: current.wishes.filter((wish) => wish.id !== result.wishId),
          });
          this.success.set(this.content.deleted);
        } else if (
          validWish(result.wish, current.event.id) &&
          result.wish.userId === userId
        ) {
          const wishes =
            result.mode === 'create'
              ? [
                  result.wish,
                  ...current.wishes.filter(
                    (wish) => wish.id !== result.wish.id,
                  ),
                ]
              : current.wishes.map((wish) =>
                  wish.id === result.wish.id ? result.wish : wish,
                );
          this.data.set({ ...current, wishes });
          this.success.set(
            result.mode === 'create'
              ? this.content.created
              : this.content.updated,
          );
        }
      });
  }
}
