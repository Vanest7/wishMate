import { HttpErrorResponse } from '@angular/common/http';
import { EventResponse, GroupResponse } from '@wishmate/api-client';
import { eventsContent } from './events.content';

export type EventStatus = NonNullable<EventResponse['status']>;
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
    (event.status === 'DRAFT' ||
      event.status === 'ACTIVE' ||
      event.status === 'CLOSED' ||
      event.status === 'ARCHIVED')
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
  const content = eventsContent.errors;
  if (!(error instanceof HttpErrorResponse)) return content.generic;
  switch (error.status) {
    case 0:
      return content.network;
    case 400:
      return content.validation;
    case 403:
      return content.forbidden;
    case 404:
      return content.missing;
    case 409:
      return content.conflict;
    default:
      return content.generic;
  }
}
