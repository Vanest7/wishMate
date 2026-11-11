import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { publicLayoutContent } from './public-layout-content';
import { ThemeSelector } from './theme-selector';

@Component({
  selector: 'app-public-layout',
  imports: [RouterLink, RouterLinkActive, RouterOutlet, ThemeSelector],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './public-layout.html',
  styleUrl: './public-layout.scss',
})
export class PublicLayout {
  readonly content = publicLayoutContent;
}
