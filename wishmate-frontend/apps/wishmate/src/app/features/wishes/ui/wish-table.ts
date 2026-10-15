import {
  ChangeDetectionStrategy,
  Component,
  input,
  output,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { WishCardModel } from '../models/wish-card.model';
import { ValidWish } from '../wish-utils';
import { wishesContent } from '../wishes.content';

@Component({
  selector: 'app-wish-table',
  imports: [RouterLink, MatButtonModule],
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
}
