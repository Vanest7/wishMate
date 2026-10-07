import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { Params, RouterLink } from '@angular/router';

@Component({
  selector: 'app-navigation-card',
  imports: [RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <a [routerLink]="destination()" [queryParams]="queryParams()">
      <span class="icon" aria-hidden="true">{{ icon() }}</span>
      <h2>{{ title() }}</h2>
      <p>{{ description() }}</p>
      <span class="action"
        >{{ actionLabel() }} <span aria-hidden="true">→</span></span
      >
    </a>
  `,
  styleUrl: './navigation-card.scss',
})
export class NavigationCard {
  readonly title = input.required<string>();
  readonly description = input.required<string>();
  readonly icon = input.required<string>();
  readonly destination = input.required<string>();
  readonly queryParams = input<Params | null>(null);
  readonly actionLabel = input('Abrir');
}
