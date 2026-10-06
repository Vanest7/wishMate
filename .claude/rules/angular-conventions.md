---
paths:
  - "wishmate-frontend/apps/**/*.ts"
  - "wishmate-frontend/apps/**/*.html"
  - "wishmate-frontend/apps/**/*.scss"
  - "wishmate-frontend/libs/**/*.ts"
---
# Angular coding conventions

Apply these to handwritten code, not generated API files. Preserve existing root
files such as `app.ts`; avoid unrelated renames.

## Naming

| Item | File example | Export example |
| --- | --- | --- |
| Routed page | `group-list-page.ts` | `GroupListPage` |
| Form component | `group-form.ts` | `GroupForm` |
| Presentation component | `wish-card.ts` | `WishCard` |
| Data service | `groups-data.service.ts` | `GroupsDataService` |
| Feature state/facade, if needed | `groups.facade.ts` | `GroupsFacade` |
| Route configuration | `groups.routes.ts` | `groupsRoutes` |
| Functional guard | `auth.guard.ts` | `authGuard` |
| Functional interceptor | `auth.interceptor.ts` | `authInterceptor` |
| Pure permission helpers | `group-permissions.ts` | `canRemoveMember` |
| View model | `wish-card.model.ts` | `WishCardModel` |
| Colocated test | `wish-card.spec.ts` | Behavioral descriptions |

Use kebab-case files, PascalCase types/classes, camelCase functions/properties and
meaningful names. Match templates/styles to the component basename. Use `app-`
component selectors and `app` camelCase directive selectors, as required by the app's
ESLint configuration. Avoid `I` interface prefixes and generic `helper.service.ts` names.

## Components and state

- Use standalone components, explicit template imports and lazy feature routes.
  Do not add SharedModule or routing NgModules to reproduce the old project.
- Prefer `inject`, signal inputs/outputs where appropriate, and `OnPush` for new
  components. Use signals for local/derived UI state and RxJS for async request flows.
- Use `computed` for derived state; do not copy the same value into multiple stores.
- Use built-in template control flow; track lists by stable entity ID, not mutable index.
- Avoid template method calls that allocate collections or perform business work.
- Keep view/edit reuse where the UI is genuinely the same. Do not force every CRUD
  feature into a generic component or generate a table framework in advance.
- Write explicit types for public boundaries. Avoid `any`, unjustified `!` assertions,
  unsafe casts and ESLint suppression. Existing loose compiler settings are not a
  reason to write untyped new code; do not globally enable strict mode in an unrelated task.

## Forms and async behavior

- Use typed Reactive Forms and Angular Material. Use nonnullable controls when the
  domain requires a value; preserve nullable optional fields such as price and dates.
- Trim suitable names/descriptions, reject whitespace-only required fields, and
  preserve passwords exactly. Derive limits from request DTOs and business rules.
- Map form values explicitly to generated request DTOs. Exclude IDs, role, owner or
  status fields that the endpoint does not accept. PUT is not an arbitrary partial PATCH.
- Show validation after touch/submit; mark invalid controls touched on submit.
- Represent loading, empty, error and success separately. Do not turn request failures
  into empty arrays that look like successful empty results.
- Use `switchMap` for changing route/search reads. Do not use it to cancel writes
  blindly. Disable pending submission or use an intentional write operator such as
  `exhaustMap` to prevent duplicate actions.
- Avoid nested subscriptions. Use the async pipe, signal interop or
  `takeUntilDestroyed` with a valid destruction context for ongoing subscriptions.
- Restore pending state on success and failure. Do not retry mutations automatically.
- Confirm destructive user actions, preserve forms on failure, and refresh relevant
  state only after successful writes. Treat 204 responses as successful empty bodies.

## UI and accessibility

- Use Angular Material controls/dialogs and Tailwind for layout; keep local SCSS for
  component-specific styles. Do not introduce PrimeNG, Bootstrap or Formly by default.
- Provide labels, keyboard access, meaningful button types, focus handling and
  accessible loading/error feedback. Icon-only actions need accessible names.
- Display numeric values using Spanish locale where appropriate; send numbers, not
  formatted currency strings, to the API. Do not apply timezone conversion to LocalDate.
- No translation library is currently installed. Keep Spanish UI copy consistent;
  do not invent translation pipes or recreate proprietary translation services.
  Add i18n as a deliberate feature before requiring translation keys everywhere.
- Safely render user content as text. Validate external link protocols; use appropriate
  protection for new-tab links. Do not bypass Angular sanitization for wish content.
