import { EventAction, ValidEvent } from '../event-utils';

export type EventDialogData =
  | { mode: 'create'; groupId: number }
  | { mode: EventAction; groupId: number; event: ValidEvent };

export type EventFormDialogData =
  | { mode: 'create'; groupId: number }
  | { mode: 'edit'; groupId: number; event: ValidEvent };
export type EventTransitionDialogData = {
  mode: Exclude<EventAction, 'edit'>;
  groupId: number;
  event: ValidEvent;
};
