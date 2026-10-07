import { isEventStatus } from '../../shared/domain/event-status';
import { apiErrorMessage } from '../../core/http/api-error';
import { EventResponse, GroupResponse } from '@wishmate/api-client';
import { eventsContent } from './events.content';

import { EventStatus } from '../../shared/domain/event-status';
export type { EventStatus } from '../../shared/domain/event-status';
export type EventAction = 'edit' | 'activate' | 'close' | 'archive';
export type ValidEvent = EventResponse & {
  id: number;
  groupId: number;
  name: string;
  status: EventStatus;
};

export interface EventGroupContext {
  group: GroupResponse & { id: number };
}

export function validEvent(
  event: EventResponse,
  groupId: number,
): event is ValidEvent {
  return (
    !!event &&
    Number.isSafeInteger(event.id) &&
    (event.id ?? 0) > 0 &&
    event.groupId === groupId &&
    typeof event.name === 'string' &&
    !!event.name.trim() &&
    isEventStatus(event.status)
  );
}

export function canEventAction(
  status: EventResponse['status'],
  action: EventAction,
): boolean {
  switch (action) {
    case 'edit':
      return status === 'DRAFT' || status === 'ACTIVE';
    case 'activate':
      return status === 'DRAFT';
    case 'close':
      return status === 'ACTIVE';
    case 'archive':
      return status === 'CLOSED';
  }
}

export function validDateOnly(value: string): boolean {
  if (value === '') return true;
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value) || value.startsWith('0000'))
    return false;
  const date = new Date(value + 'T00:00:00Z');
  return (
    !Number.isNaN(date.getTime()) && date.toISOString().slice(0, 10) === value
  );
}

export function formatEventDate(value: string | null | undefined): string {
  if (!value || !validDateOnly(value)) return '—';
  const [year, month, day] = value.split('-');
  return `${day}/${month}/${year}`;
}

export function eventError(error: unknown): string {
  return apiErrorMessage(error, eventsContent.errors);
}
