import { positiveId } from '../../shared/util/positive-id';
export { positiveId } from '../../shared/util/positive-id';
import { apiErrorMessage } from '../../core/http/api-error';
import {
  EventResponse,
  GiftReservationResponse,
  GroupResponse,
  WishResponse,
} from '@wishmate/api-client';

export type Reservation = GiftReservationResponse & {
  id: number;
  wishId: number;
};
export interface ReservationTarget {
  group: GroupResponse & { id: number; name: string };
  event: EventResponse & { id: number; groupId: number; name: string };
  wish: WishResponse & {
    id: number;
    eventId: number;
    userId: number;
    name: string;
  };
}
export interface ReservationCard {
  reservation: Reservation;
  target: ReservationTarget | null;
  date: string | null;
}
export function validReservation(
  value: GiftReservationResponse,
): value is Reservation {
  return !!value && positiveId(value.id) && positiveId(value.wishId);
}
export function validTarget(
  group: GroupResponse,
  event: EventResponse,
  wish: WishResponse,
  groupId: number,
  eventId: number,
  wishId: number,
): boolean {
  return (
    !!group &&
    !!event &&
    !!wish &&
    group.id === groupId &&
    event.id === eventId &&
    event.groupId === groupId &&
    wish.id === wishId &&
    wish.eventId === eventId &&
    positiveId(wish.userId) &&
    [group.name, event.name, wish.name].every(
      (name) => typeof name === 'string' && !!name.trim(),
    )
  );
}
export function canReserve(
  target: ReservationTarget,
  userId: number | null,
): boolean {
  return (
    positiveId(userId) &&
    target.wish.userId !== userId &&
    target.event.status === 'ACTIVE'
  );
}
const reservationDateFormatter = new Intl.DateTimeFormat('es-ES', {
  dateStyle: 'medium',
});

export function reservationDate(value: string | undefined): string | null {
  if (!value) return null;
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? null
    : reservationDateFormatter.format(date);
}
export function reservationError(error: unknown): string {
  return apiErrorMessage(error, {
    generic: 'No hemos podido completar la operación. Inténtalo de nuevo.',
    network:
      'No se ha podido conectar. Comprueba tu conexión. Si estabas reservando, consulta Mis reservas antes de volver a intentarlo.',
    validation:
      'El estado del evento o los datos ya no permiten esta operación.',
    unauthorized: 'Inicia sesión de nuevo para continuar.',
    forbidden:
      'No puedes realizar esta operación. Tus permisos o tu reserva pueden haber cambiado.',
    missing: 'El deseo, el evento o la reserva ya no están disponibles.',
    conflict:
      'No se ha podido completar la operación: el deseo puede estar reservado o su estado ha cambiado. Consulta Mis reservas antes de volver a intentarlo.',
  });
}
