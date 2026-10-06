# Frontend architecture — SCRUM-27

## Scope

This change establishes the application structure and navigation. Feature pages are
explicit placeholders, not working authentication or CRUD screens. No backend or
API contract changes are included. The generated client remains unchanged.

## Structure and dependencies

All application paths are relative to `apps/wishmate/src/app/`.

| Location                         | Responsibility                                               |
| -------------------------------- | ------------------------------------------------------------ |
| `app.config.ts`, `app.routes.ts` | Providers and lazy route composition                         |
| `core/auth/`                     | In-memory session, route guards and safe return destinations |
| `core/routing/`                  | Resource ID syntax validation                                |
| `layout/`                        | Public and authenticated shells                              |
| `features/auth/pages/`           | Authentication route entry points                            |
| `features/groups/pages/`         | Group route entry point                                      |
| `features/events/pages/`         | Group-scoped event route entry point                         |
| `features/wishes/pages/`         | Group/event-scoped wish route entry point                    |
| `features/reservations/pages/`   | Personal reservation route entry point                       |
| `shared/ui/`                     | Domain-neutral not-found page                                |

Pages will consume their feature's `data-access/` and `ui/` when those have actual
responsibilities. Data access will consume `@wishmate/api-client` and core services.
Core and shared UI must not import feature internals. Features navigate to one
another using routes. Do not add empty directories, pass-through repositories,
new Nx libraries, or global business stores for this task. Keep internal imports
relative. Existing Nx boundaries do not enforce these app-internal conventions.

Use generated DTOs directly unless presentation requires a genuinely different
shape. The private session model uses an absolute expiration timestamp, unlike
the login response's relative lifetime; it does not duplicate a transport DTO.

## Routes

| URL                                       | Access        | Context                                 |
| ----------------------------------------- | ------------- | --------------------------------------- |
| `/`                                       | Redirect      | `/groups` then authentication if needed |
| `/auth/login`                             | Public        | Optional validated `returnUrl`          |
| `/auth/register`                          | Public        | Preserves `returnUrl` back to login     |
| `/groups`                                 | Authenticated | Current session                         |
| `/groups/:groupId/events`                 | Authenticated | Group ID from URL                       |
| `/groups/:groupId/events/:eventId/wishes` | Authenticated | Both IDs from URL                       |
| `/reservations`                           | Authenticated | Current session                         |
| `/not-found`, unknown URLs                | Public        | No resource data                        |

Features and pages use lazy imports. Component input binding and inherited route
parameters allow direct links and updates between parent IDs without a selected
group store. IDs must be positive safe integers. The guard validates syntax only;
backend membership and parent-child association checks remain authoritative.
A real API 404 must be handled by the future data-access/page implementation.

## Session policy and remaining authentication integration

The token exists only in `SessionService` memory. A hard reload creates an anonymous
session. A private deep link redirects to login with its original URL, query and
fragment in `returnUrl`. The URL survives login/register navigation. There is no
persistent browser token, silent refresh, server logout call or authentication bypass.
A local logout clears the session and exits the private shell. Timed expiration
also clears it; guard/token reads recheck the deadline even if timers were delayed.

The future authentication feature must:

1. Implement the actual CSRF acquisition and login using the verified backend contract.
2. Narrow optional generated login response fields; validate token type and lifetime.
3. Call `session.start(accessToken, expiresIn)` only after successful authentication.
4. Navigate to `safeReturnUrl(returnUrl)` with `replaceUrl: true`.
5. Use an API-scoped interceptor to retrieve the token via `getAccessToken()` and
   attach Bearer/CSRF credentials only to the intended API.
6. Clear session-scoped data on logout, expiration and account changes. Route/page
   providers should own business data; do not create unkeyed root caches.

The routing tests inject a test session to exercise private screens. Production
pages cannot create one yet. Consequently, real login -> private page -> reload ->
login -> original destination cannot be accepted end-to-end until login is integrated.
Guards provide navigation UX, not server authorization or JWT signature validation.
Hosting must serve the SPA entry point for frontend deep links; no hosting
configuration is part of this archive.

## UI state contract

Each real feature collection will own one discriminated state signal, initially
`loading`. Do not use independent booleans that can contradict each other.

| Status    | Meaning                                                | Visible feedback                                 |
| --------- | ------------------------------------------------------ | ------------------------------------------------ |
| `loading` | Read in progress for the current session and route IDs | Accessible progress; no stale parent data        |
| `empty`   | Successful response containing zero items              | Intentional empty message, permitted next action |
| `success` | Successful response containing one or more items       | Render the collection                            |
| `error`   | Failed request                                         | Safe message and deliberate retry of the read    |

A feature-local type may use this shape, replacing `GroupResponse` with its actual
import from `@wishmate/api-client` when the group list is implemented:

```typescript
type GroupsState = { status: 'loading' } | { status: 'empty' } | { status: 'success'; groups: readonly GroupResponse[] } | { status: 'error'; message: string };
```

This is a documented contract, not a generic store to create in advance. The current
placeholder pages have no request and must not report a fabricated empty/success result.
Use `computed` for derived UI values. Use RxJS `switchMap` for changing route/search
reads so old requests cannot update a new parent's view; emit `loading` inside each
new read and catch errors inside that read to keep future route changes working.
Use `toSignal`, the async pipe, or `takeUntilDestroyed` for subscription cleanup.
Do not use `catchError(() => of([]))`, nested subscriptions or automatic mutation retries.
Keep submit state separate from collection state and preserve forms after errors.

## Verification and follow-up

Automated coverage includes anonymous deep-link preservation, both parent IDs and
navigation between them, invalid IDs, unknown routes, private child protection,
registration return links, expiration, clock jumps, session replacement, no session
restoration in a new instance, and rejected return destinations.

The unit/router suite does not replace browser/backend E2E testing. Authentication,
API errors and the four collection states need feature-level tests once real reads
and forms exist. Do not mark those integrations complete based on the placeholder pages.
