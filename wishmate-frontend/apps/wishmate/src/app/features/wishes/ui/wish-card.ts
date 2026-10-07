import {
  ChangeDetectionStrategy,
  Component,
  input,
  output,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { WishCardModel } from '../models/wish-card.model';
import { wishesContent } from '../wishes.content';

@Component({
  selector: 'app-wish-card',
  imports: [RouterLink, MatButtonModule],
  templateUrl: './wish-card.html',
  styleUrl: './wish-card.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WishCard {
  readonly model = input.required<WishCardModel>();
  readonly groupId = input.required<number>();
  readonly eventId = input.required<number>();
  readonly canReserve = input(false);
  readonly disabled = input(false);
  readonly editRequested = output<void>();
  readonly deleteRequested = output<void>();
  readonly content = wishesContent;
}
