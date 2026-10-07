import {
  ChangeDetectionStrategy,
  Component,
  input,
  output,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { ReservationCard as ReservationCardModel } from '../reservation-utils';
import { eventStatusLabels } from '../../../shared/domain/event-status';

@Component({
  selector: 'app-reservation-card',
  imports: [RouterLink, MatButtonModule],
  templateUrl: './reservation-card.html',
  styleUrl: './reservation-card.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReservationCard {
  readonly model = input.required<ReservationCardModel>();
  readonly canCancel = input(false);
  readonly disabled = input(false);
  readonly cancelRequested = output<void>();
  readonly retryRequested = output<void>();
  readonly statusLabels = eventStatusLabels;
}
