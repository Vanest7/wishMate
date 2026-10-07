import { HttpErrorResponse } from '@angular/common/http';
import {
  EventResponse,
  GroupMemberResponse,
  GroupResponse,
  WishResponse,
} from '@wishmate/api-client';
import { wishesContent } from './wishes.content';

export type WishEvent = EventResponse & {
  id: number;
  groupId: number;
  name: string;
  status: NonNullable<EventResponse['status']>;
};
export type ValidWish = WishResponse & {
  id: number;
  eventId: number;
  userId: number;
  name: string;
  description: string;
};
export interface WishPageData {
  group: GroupResponse & { id: number; name: string };
  event: WishEvent;
  wishes: ValidWish[];
  members: GroupMemberResponse[];
}

export function positiveId(value: unknown): value is number {
  return typeof value === 'number' && Number.isSafeInteger(value) && value > 0;
}
export function validWishEvent(
  event: EventResponse,
  groupId: number,
  eventId: number,
): event is WishEvent {
  return (
    !!event &&
    event.id === eventId &&
    event.groupId === groupId &&
    typeof event.name === 'string' &&
    !!event.name.trim() &&
    (event.status === 'DRAFT' ||
      event.status === 'ACTIVE' ||
      event.status === 'CLOSED' ||
      event.status === 'ARCHIVED')
  );
}
export function validWish(
  wish: WishResponse,
  eventId: number,
): wish is ValidWish {
  return (
    !!wish &&
    positiveId(wish.id) &&
    positiveId(wish.userId) &&
    wish.eventId === eventId &&
    typeof wish.name === 'string' &&
    !!wish.name.trim() &&
    typeof wish.description === 'string' &&
    !!wish.description.trim()
  );
}
export function canManageWish(
  wish: WishResponse,
  event: EventResponse,
  userId: number | null,
): boolean {
  return (
    positiveId(userId) &&
    event.status === 'ACTIVE' &&
    wish.eventId === event.id &&
    wish.userId === userId
  );
}
export function safePurchaseUrl(
  value: string | null | undefined,
): string | null {
  if (!value || value.length > 2048) return null;
  try {
    const url = new URL(value.trim());
    return (url.protocol === 'https:' || url.protocol === 'http:') &&
      !!url.hostname &&
      !url.username &&
      !url.password
      ? url.href
      : null;
  } catch {
    return null;
  }
}
export function parsePrice(value: string): number | undefined | null {
  const trimmed = value.trim();
  if (!trimmed) return undefined;
  if (!/^\d{1,8}([.,]\d{1,2})?$/.test(trimmed)) return null;
  const price = Number(trimmed.replace(',', '.'));
  return Number.isFinite(price) && price >= 0 && price <= 99999999.99
    ? price
    : null;
}
const priceFormatter = new Intl.NumberFormat('es-ES', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
  useGrouping: true,
});
export function formatPrice(price: number | null | undefined): string {
  return typeof price === 'number' && Number.isFinite(price) && price >= 0
    ? priceFormatter.format(price)
    : '—';
}
export function wishError(error: unknown): string {
  const content = wishesContent.errors;
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
