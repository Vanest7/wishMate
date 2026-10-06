---
paths:
  - "wishmate-frontend/**/*"
---
# Frontend architecture

The app is currently a shell with an API library. The following layout is the
convention for NEW code, not a claim these feature directories already exist.
All paths below are relative to `wishmate-frontend/`.

| Location | Responsibility |
| --- | --- |
| `apps/wishmate/src/app/app.config.ts` | Global providers and infrastructure composition |
| `apps/wishmate/src/app/app.routes.ts` | Top-level routes and lazy feature entry points |
| `apps/wishmate/src/app/core/auth/` | Session state, auth guards and auth/CSRF integration |
| `apps/wishmate/src/app/core/http/` | Shared transport/error normalization |
| `apps/wishmate/src/app/core/config/` | Runtime/build configuration, without secrets |
| `apps/wishmate/src/app/layout/` | Application shell and navigation |
| `apps/wishmate/src/app/shared/ui/` | Domain-neutral reusable presentation |
| `apps/wishmate/src/app/shared/util/` | Small pure cross-feature helpers |
| `apps/wishmate/src/app/features/<feature>/pages/` | Routed page orchestration |
| `apps/wishmate/src/app/features/<feature>/ui/` | Feature-specific presentation and forms |
| `apps/wishmate/src/app/features/<feature>/data-access/` | API coordination and feature state |
| `apps/wishmate/src/app/features/<feature>/models/` | View models only, when different from DTOs |
| `apps/wishmate/src/app/features/<feature>/<feature>.routes.ts` | Feature route definitions |
| `libs/api-client/src/index.ts` | Public API client entry point |
| `libs/api-client/src/lib/generated/` | Generated transport/models, never handwritten |

Use `auth`, `groups`, `events`, `wishes`, `reservations` as feature names. Create
only the folders a task needs. Do not create an empty profile feature for missing APIs.

## Dependency direction

- App composition imports feature routes. Pages consume their feature's data access
  and UI. Data access consumes `@wishmate/api-client` and core infrastructure.
- Domain-neutral shared UI must not import features, API services or session state.
- Core must not import feature pages. Features must not reach into another feature's
  internals; navigate by routes or extract a narrowly shared capability when justified.
- Generated code must never depend on app code. Import its public symbols through
  `@wishmate/api-client`, not relative paths into generated internals.
- Keep app-internal imports relative for now. The only verified alias is
  `@wishmate/api-client`; explicitly configure any new alias before using it.
- Add a service/facade for real coordination or shared state, not a chain of
  pass-through wrappers. Keep API calls out of presentation-only components.
- Prefer feature/route-scoped state for group/event data. Key cached data by session
  and parent IDs, invalidate it after mutations, and clear it at logout/account switch.

## Nx boundaries and growth

Do not generate three Nx libraries per business entity. Start within the app so its
existing test target includes the tests. Extract a library when independently reused
or when an enforceable boundary has concrete value. Configure its alias, public API,
tags, lint and test coverage together; do not assume the generator provides a test target.
The reviewed Nx library generator defaults to `unitTestRunner: none`.

Current ESLint module-boundary tags allow any tag to depend on any tag. Therefore
the conventions above are reviewed rules, not currently enforced restrictions.
Do not claim that this documentation or adding tags alone makes them enforced.
