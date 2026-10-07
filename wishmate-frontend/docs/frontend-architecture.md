# Frontend architecture

## Organization

The Angular application is organized by feature within `apps/wishmate/src/app`.
The generated transport client is a separate Nx library, consumed through
`@wishmate/api-client`. This refactor changes no backend code or API contracts.

| Location                                 | Responsibility                                                                 |
| ---------------------------------------- | ------------------------------------------------------------------------------ |
| `app.routes.ts`, `app.config.ts`         | Lazy route composition and global providers                                    |
| `core/auth`, `core/routing`, `core/http` | Authentication, route validation and HTTP error classification                 |
| `layout`                                 | Public and authenticated application shells                                    |
| `features/*/pages`                       | Route orchestration, view state and dialog coordination                        |
| `features/*/data-access`                 | Coordinated API reads and response validation                                  |
| `features/*/ui`                          | Feature presentation, forms and dedicated confirmations                        |
| `features/*/models`                      | Presentation and dialog contracts                                              |
| `shared/ui`                              | Domain-neutral reusable presentation                                           |
| `shared/util`                            | Domain-neutral pure utilities                                                  |
| `shared/domain`                          | Narrow shared domain definitions, currently event status labels and validation |

`WishesDataService`, `GroupsDataService` and `DashboardDataService` are scoped to
the pages that provide them. They coordinate or validate real reads; there are no
root business caches or pass-through repository layers. `ReservationsDataService`
retains its existing page scope and current backend contract.

## Component boundaries

`WishCard` and `ReservationCard` receive typed presentation inputs and emit actions.
They do not inject HTTP services or session state. Their styles are colocated with
their templates, including responsive and accessible states. Pages retain the list
semantics, stable entity tracking and mutation coordination.

`GroupSettingsMenu` owns hover/menu interaction and timer cleanup. The parent
continues to validate permissions before executing an action. Member presentation
permissions derive the acting role once per context/session update; the click
handler still checks current permissions before submitting.

`WishDialog` and `EventDialog` handle create/edit forms. `DeleteWishDialog` and
`EventTransitionDialog` handle confirmations without allocating forms. Confirmations
preserve pending guards, failure feedback, permission/state checks and cleanup.

A small single-resource page can continue to use a generated API service directly.
Extract data access when coordination or validation has a concrete responsibility;
do not add a service just to forward a call.

## State and business safeguards

- Signals hold local state; computed values derive presentation and filtering.
- Route/session-driven reads use `switchMap` and destruction cleanup.
- Loading, errors and successful empty results remain distinct in the UI.
- Mutations are explicitly confirmed where destructive, protected against duplicate
  submission and never retried automatically.
- Only owners edit/delete their wishes in active events. Own-wish cards never expose
  reservation status. Unknown reservation details are not treated as availability.
- Guards and UI permissions do not replace backend authorization.
- HTTP error classification is shared, but each feature retains its own messages,
  including reservation-specific guidance for uncertain mutation outcomes.

## Enforced imports

`apps/wishmate/eslint.config.mjs` rejects imports between feature internals,
core-to-feature/layout imports, data-access-to-presentation imports and domain/API
imports from shared UI. It also rejects direct imports of generated implementation
files. Integration tests that consume a feature service live with that feature.

Nx tags restrict app dependencies to data-access libraries and prevent data-access
libraries from depending on app projects. Folder-level rules complement these
project-level restrictions. Keep the feature list in the app ESLint configuration
up to date when adding a new feature. New aliases need matching boundary rules.

## Deferred backend optimization

The reservation list still resolves parent information by reading visible groups,
events and wishes. Reducing that request fan-out requires extending the reservation
read contract with authorized parent/display data. That change is deliberately
outside this frontend refactor. Unresolved reservations remain visible and no new
availability endpoint is assumed.

## Verification

Run `npm run check` from `wishmate-frontend`. It checks formatting, ESLint, Vitest
and the production build. Tests cover page privacy and context changes, card actions,
confirmation mutations and errors, and API error classification. Unit tests are not
a substitute for a browser/backend end-to-end run.
