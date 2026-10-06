# Business rules: verified backend behavior

These rules apply to WishMate business work, including frontend permissions and tests.
Sources below are relative to `demo/src/main/java/com/gifts/demo/`. Re-read the
invoked code when the repository has changed. Do not substitute old conversation
endpoint lists for the current contract.

## Groups and memberships

Roles are group-specific: `OWNER`, `ADMIN`, `MEMBER`. A role in one group grants
nothing in another. Creating a group creates its owner's membership atomically.

| Action | OWNER | ADMIN | MEMBER |
| --- | --- | --- | --- |
| Read group and members | Yes | Yes | Yes |
| Rename group | Yes | Yes | No |
| Add an existing user as MEMBER | Yes | Yes | No |
| Remove another MEMBER | Yes | Yes | No |
| Remove another ADMIN | Yes | No | No |
| Change another non-owner's role to ADMIN or MEMBER | Yes | No | No |
| Leave through the dedicated leave endpoint | No | Yes | Yes |
| Delete group | Yes | No | No |

Never remove/demote the owner or offer ownership transfer: no such operation is
implemented. Self-removal must use the leave endpoint, including for administrators.
Adding a member takes a positive existing `userId`; do not invent email invitations
or user-search endpoints. Duplicate membership is a conflict.

Read `controller/GroupController.java`, `controller/GroupMemberController.java`,
`domain/usecase/group/impl/` and `domain/service/impl/GroupAccessServiceImpl.java`.
The controllers invoke group use cases directly. Some similarly named methods in
`GroupServiceImpl` have different checks; do not use those as the permission contract.
`GroupPermissionServiceImpl` grants ownership through membership role; the access
service additionally protects the creator when identifying an owner target.

## Events

Every current group member may create, update and transition events. The service
does not require OWNER, ADMIN or event creator privileges. Do not impose those
restrictions in the UI unless the product/backend contract is deliberately changed.

| State | Edit event fields | Create/edit/delete own wishes | Reserve/cancel own reservation | Next transition |
| --- | --- | --- | --- | --- |
| DRAFT | Yes | No | No | Activate -> ACTIVE |
| ACTIVE | Yes | Yes | Yes | Close -> CLOSED |
| CLOSED | No | No | No | Archive -> ARCHIVED |
| ARCHIVED | No | No | No | None |

Creation accepts DRAFT or ACTIVE; missing status defaults to ACTIVE. Reads are not
restricted by event state, but group membership and nested resource checks apply.
There is no reopen or event-delete endpoint in the reviewed contract. Update uses
PUT and does not accept a status field: use dedicated transition endpoints.
Dates do not automatically change status. Do not infer status from today's date.

Sources: `domain/service/impl/EventServiceImpl.java`, `EventStateServiceImpl.java`,
and `controller/EventController.java`.

## Wishes

- Creating a wish assigns the authenticated requester as its owner, in an ACTIVE event.
- Members may read wishes in their group. Filtering by user also checks that user's membership.
- Only the wish owner may edit/delete it, and only in ACTIVE events. Group owners
  and administrators have no override for another person's wish.
- Validate group -> event -> wish association. Never accept a valid ID from a different parent.
- The current wish update/delete checks do not depend on reservation existence.
  Do not disable owner actions based on reservation data: this would disclose a surprise.

Source: `domain/service/impl/WishServiceImpl.java` and `controller/WishController.java`.

## Reservations and surprise privacy

- Reserving requires group membership, an ACTIVE event and another user's wish.
- An already reserved wish produces a conflict; handle races without claiming success.
- Cancelling also requires ACTIVE state, membership and the current user's reservation.
  Neither ADMIN nor OWNER can cancel someone else's reservation.
- Own-wish reservation/cancellation is rejected before reading reservation data.
- The wish owner must not learn whether a reservation exists, who reserved it, or when.
  Do not expose this through badges, counts, disabled actions, tooltips, notifications,
  accessibility text, client logs or retained state from another session.
- `GET /api/reservations` lists the caller's reservations. `GET /api/reservations/{reservationId}`
  returns a reservation only to its reserver. These reads currently do not perform
  group membership or event-state checks; do not claim otherwise.
- `getVisibleStatus` internally returns no status for the wish owner; other users may
  receive AVAILABLE, RESERVED_BY_ME or RESERVED. It is NOT exposed by the reviewed
  controllers/OpenAPI. `WishResponse` also has no reservation status field.
- Missing status means unknown, not AVAILABLE. Never probe availability by reserving
  or cancelling. A status badge feature needs an explicit backend contract addition.
- Do not assume leave/remove membership cleans up reservations: inspect persistence
  behavior before promising cleanup. Former-member reservations can remain listed,
  while cancellation requires membership. Treat this as a product/backend question.

Source: `domain/service/impl/GiftReservationServiceImpl.java`,
`controller/GiftReservationController.java`, and `controller/webmodel/wish/WishResponse.java`.

## Form constraints

| Input | Current constraint |
| --- | --- |
| Group name | Required, nonblank, maximum 100 characters |
| Event name | Required, nonblank, maximum 100 characters |
| Event description | Optional, maximum 255 characters |
| Event dates | Optional; end must not precede start when both are supplied |
| Wish name | Required, nonblank, maximum 150 characters |
| Wish description | Required, nonblank, maximum 255 characters |
| Purchase URL | Optional, maximum 2048 characters |
| Estimated price | Optional, 0 to 99999999.99, at most two decimal places |
| Login | Email and password; no userName field |
| Registration | userName, email and password; all required, email must be valid |

Use request DTO annotations and invoked services together. Do not invent a password
policy or required price/link. For link rendering permit only HTTP(S); that is a UI
safety convention, not a claim that the backend currently validates URL schemes.
Preserve date-only values as `YYYY-MM-DD` without timezone conversion.
