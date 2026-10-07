import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  input,
  output,
} from '@angular/core';
import { MatMenuModule, MatMenuTrigger } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { groupPermissions } from '../group-utils';
import { groupsContent } from '../groups.content';

@Component({
  selector: 'app-group-settings-menu',
  imports: [MatMenuModule, MatButtonModule],
  templateUrl: './group-settings-menu.html',
  styleUrl: './group-settings-menu.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GroupSettingsMenu {
  readonly permissions = input.required<ReturnType<typeof groupPermissions>>();
  readonly disabled = input(false);
  readonly renameRequested = output<void>();
  readonly deleteRequested = output<void>();
  readonly content = groupsContent;
  constructor() {
    inject(DestroyRef).onDestroy(() => this.cancelSettingsClose());
  }
  private settingsCloseTimer: ReturnType<typeof setTimeout> | undefined;

  openSettings(trigger: MatMenuTrigger): void {
    this.cancelSettingsClose();
    if (this.disabled() || trigger.menuOpen) return;

    trigger.openMenu();
    trigger.menu?.focusFirstItem('mouse');
  }

  cancelSettingsClose(): void {
    clearTimeout(this.settingsCloseTimer);
    this.settingsCloseTimer = undefined;
  }

  scheduleSettingsClose(trigger: MatMenuTrigger): void {
    this.cancelSettingsClose();
    this.settingsCloseTimer = setTimeout(() => trigger.closeMenu(), 250);
  }
}
