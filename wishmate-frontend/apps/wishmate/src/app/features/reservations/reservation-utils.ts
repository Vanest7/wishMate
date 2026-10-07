import { HttpErrorResponse } from '@angular/common/http';
import {
  EventResponse,
  GiftReservationResponse,
  GroupResponse,
  WishResponse,
} from '@wishmate/api-client';

export function positiveId(value: unknown): value is number {
  return typeof value === 'number' && Number.isSafeInteger(value) && value > 0;
}
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
export function reservationDate(value: string | undefined): string | null {
  if (!value) return null;
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? null
    : new Intl.DateTimeFormat('es-ES', { dateStyle: 'medium' }).format(date);
}
export function reservationError(error: unknown): string {
  if (!(error instanceof HttpErrorResponse))
    return 'No hemos podido completar la operación. Inténtalo de nuevo.';
  switch (error.status) {
    case 0:
      return 'No se ha podido conectar. Comprueba tu conexión. Si estabas reservando, consulta Mis reservas antes de volver a intentarlo.';
    case 400:
      return 'El estado del evento o los datos ya no permiten esta operación.';
    case 401:
      return 'Inicia sesión de nuevo para continuar.';
    case 403:
      return 'No puedes realizar esta operación. Tus permisos o tu reserva pueden haber cambiado.';
    case 404:
      return 'El deseo, el evento o la reserva ya no están disponibles.';
    case 409:
      return 'No se ha podido completar la operación: el deseo puede estar reservado o su estado ha cambiado. Consulta Mis reservas antes de volver a intentarlo.';
    default:
      return 'No hemos podido completar la operación. Inténtalo de nuevo.';
  }
}
