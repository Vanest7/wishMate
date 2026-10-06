# API contract snapshot

Source: `wishmate-frontend/openapi/wishmate.json`, commit
`152e88777349a713dce133c683810e12cd4f302c`, checked against current controllers.
All routes below are backend routes, not proposed Angular navigation URLs.
Re-read the schema and generated service before using any signature.

| Method | Path | Operation ID |
| --- | --- | --- |
| GET | `/api/auth/csrf` | `getCsrfToken` |
| POST | `/api/auth/login` | `login` |
| POST | `/api/auth/register` | `register` |
| GET | `/api/groups` | `getMyGroups` |
| POST | `/api/groups` | `createGroup` |
| GET | `/api/groups/{groupId}` | `getGroup` |
| DELETE | `/api/groups/{groupId}` | `deleteGroup` |
| PATCH | `/api/groups/{groupId}` | `renameGroup` |
| GET | `/api/groups/{groupId}/events` | `getEvents` |
| POST | `/api/groups/{groupId}/events` | `createEvent` |
| GET | `/api/groups/{groupId}/events/{eventId}` | `getEvent` |
| PUT | `/api/groups/{groupId}/events/{eventId}` | `updateEvent` |
| POST | `/api/groups/{groupId}/events/{eventId}/activate` | `activateEvent` |
| POST | `/api/groups/{groupId}/events/{eventId}/archive` | `archiveEvent` |
| POST | `/api/groups/{groupId}/events/{eventId}/close` | `closeEvent` |
| GET | `/api/groups/{groupId}/events/{eventId}/wishes` | `getWishesByEvent` |
| POST | `/api/groups/{groupId}/events/{eventId}/wishes` | `createWish` |
| GET | `/api/groups/{groupId}/events/{eventId}/wishes/user/{userId}` | `getWishesByUser` |
| GET | `/api/groups/{groupId}/events/{eventId}/wishes/{wishId}` | `getWish` |
| PUT | `/api/groups/{groupId}/events/{eventId}/wishes/{wishId}` | `updateWish` |
| DELETE | `/api/groups/{groupId}/events/{eventId}/wishes/{wishId}` | `deleteWish` |
| GET | `/api/groups/{groupId}/members` | `getMembers` |
| POST | `/api/groups/{groupId}/members` | `addMember` |
| DELETE | `/api/groups/{groupId}/members/me` | `leaveGroup` |
| DELETE | `/api/groups/{groupId}/members/{userId}` | `removeMember` |
| PATCH | `/api/groups/{groupId}/members/{userId}` | `changeRole` |
| GET | `/api/reservations` | `getMyReservations` |
| POST | `/api/reservations/groups/{groupId}/events/{eventId}/wishes/{wishId}` | `createReservation` |
| DELETE | `/api/reservations/groups/{groupId}/events/{eventId}/wishes/{wishId}` | `cancelReservation` |
| GET | `/api/reservations/{reservationId}` | `getReservation` |

## Request and response discipline

Use generated DTOs from `@wishmate/api-client`; the table is an index, not a
replacement for parameter/body types. Collections currently return arrays, not
invented paginated envelopes. Cancellation/deletion success can have a 204 empty body.
The generated reservation creation call uses path parameters, not a wishId body.

Known omissions and permission details are in `project-baseline.md` and
`../rules/business-domain.md`. In particular, there is no reservation-status endpoint,
no /api/reservations/me route, no /api/users/me, no event-delete or reopen endpoint,
and no refresh/server-logout endpoint in this schema.

Do not confuse a service method with an exposed controller operation. If a new
contract is requested, implement/document it deliberately and regenerate the client.
