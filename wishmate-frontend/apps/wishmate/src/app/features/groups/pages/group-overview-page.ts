import {
  ChangeDetectionStrategy,
  Component,
  inject,
  Signal,
} from '@angular/core';
import { ROUTER_OUTLET_DATA } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';
import { GroupSectionContext } from './group-section-context';
import { groupsContent } from '../groups.content';

@Component({
  selector: 'app-group-overview-page',
  imports: [MatButtonModule, RouterLink],
  templateUrl: './group-overview-page.html',
  styleUrl: './groups-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GroupOverviewPage {
  readonly context = inject<Signal<GroupSectionContext>>(ROUTER_OUTLET_DATA);
  readonly content = groupsContent;
}
