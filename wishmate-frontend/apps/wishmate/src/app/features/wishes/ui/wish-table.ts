import {
  ChangeDetectionStrategy,
  Component,
  computed,
  linkedSignal,
  signal,
  input,
  output,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatSortModule, Sort } from '@angular/material/sort';
import {
  MatPaginatorIntl,
  MatPaginatorModule,
  PageEvent,
} from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { WishCardModel } from '../models/wish-card.model';
import { ValidWish } from '../wish-utils';
import { wishesContent } from '../wishes.content';

function createPaginatorIntl(): MatPaginatorIntl {
  const intl = new MatPaginatorIntl();
  intl.itemsPerPageLabel = wishesContent.pagination.itemsPerPage;
  intl.nextPageLabel = wishesContent.pagination.next;
  intl.previousPageLabel = wishesContent.pagination.previous;
  intl.firstPageLabel = wishesContent.pagination.first;
  intl.lastPageLabel = wishesContent.pagination.last;
  intl.getRangeLabel = (page, size, length) => {
    if (length === 0 || size === 0) return `0 de ${length}`;
    return `${page * size + 1}–${Math.min((page + 1) * size, length)} de ${length}`;
  };
  return intl;
}

@Component({
  selector: 'app-wish-table',
  imports: [RouterLink, MatButtonModule, MatSortModule, MatPaginatorModule],
  providers: [{ provide: MatPaginatorIntl, useFactory: createPaginatorIntl }],
  templateUrl: './wish-table.html',
  styleUrl: './wish-table.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WishTable {
  readonly models = input.required<readonly WishCardModel[]>();
  readonly groupId = input.required<number>();
  readonly eventId = input.required<number>();
  readonly canReserve = input(false);
  readonly disabled = input(false);
  readonly editRequested = output<ValidWish>();
  readonly deleteRequested = output<ValidWish>();
  readonly content = wishesContent;
  readonly sort = signal<Sort>({ active: '', direction: '' });
  readonly pageSize = signal(10);
  readonly pageIndex = linkedSignal({
    source: () => this.models(),
    computation: () => 0,
  });
  private readonly collator = new Intl.Collator('es', {
    sensitivity: 'base',
    numeric: true,
  });
  readonly sortedModels = computed(() => {
    const models = this.models();
    const { active, direction } = this.sort();
    if (!direction) return models;
    const multiplier = direction === 'asc' ? 1 : -1;
    return [...models].sort((left, right) => {
      if (active === 'price') {
        const leftPrice = left.wish.estimatedPrice;
        const rightPrice = right.wish.estimatedPrice;
        // Missing prices stay last in either direction; zero is a valid price.
        if (leftPrice == null) return rightPrice == null ? 0 : 1;
        if (rightPrice == null) return -1;
        return (leftPrice - rightPrice) * multiplier;
      }
      const leftText = active === 'owner' ? left.ownerName : left.wish.name;
      const rightText = active === 'owner' ? right.ownerName : right.wish.name;
      return this.collator.compare(leftText, rightText) * multiplier;
    });
  });
  readonly visibleModels = computed(() => {
    const start = this.pageIndex() * this.pageSize();
    return this.sortedModels().slice(start, start + this.pageSize());
  });

  changeSort(sort: Sort): void {
    this.sort.set(sort);
    this.pageIndex.set(0);
  }

  changePage(event: PageEvent): void {
    this.pageSize.set(event.pageSize);
    this.pageIndex.set(event.pageIndex);
  }
}
