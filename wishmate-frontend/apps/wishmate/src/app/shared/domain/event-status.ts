import { EventResponse } from '@wishmate/api-client';

export type EventStatus = NonNullable<EventResponse['status']>;
export const eventStatusLabels = {
  DRAFT: 'Borrador',
  ACTIVE: 'Activo',
  CLOSED: 'Cerrado',
  ARCHIVED: 'Archivado',
} as const satisfies Record<EventStatus, string>;

export function isEventStatus(value: unknown): value is EventStatus {
  return (
    typeof value === 'string' &&
    Object.prototype.hasOwnProperty.call(eventStatusLabels, value)
  );
}
