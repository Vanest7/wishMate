---
paths:
  - "wishmate-frontend/**/*"
---
# Validation and tests

The application uses `@angular/build:unit-test` with Vitest. Use Vitest imports,
`vi` mocks and Angular TestBed. Do not introduce Jest APIs or ChromeHeadlessCI flags.
Colocate `.spec.ts` files within app `src/`; the reviewed app test tsconfig includes
these files. New library tests require their own configured coverage/target.

Test behavior with meaningful assertions rather than duplicating implementation.
For feature work select relevant cases:

- Loading, successful content, empty result and failed request are distinct.
- Required/whitespace/length/price/date validation and server field errors.
- Pending submissions cannot issue duplicate writes; failures preserve user input.
- Permission helpers cover role, acting user, target user and parent resource IDs.
- Own wishes never display reservation state, including accessible text.
- Group ADMIN cannot remove ADMIN; owner cannot leave; only owner changes roles.
- Event state transitions and wish/reservation mutations follow the state matrix.
- Reservation conflicts are handled without leaking private data or claiming success.
- Account/group/event changes cancel stale reads and clear or separate cached data.
- HTTP tests verify method/path/body plus API-scoped auth/CSRF headers. Third-party
  requests must not receive credentials. Test empty/nonstandard error bodies.

For new auth flows use HttpTestingController or the existing Angular HTTP testing
pattern and test provider order. Mock generated services in component tests when
testing UI behavior; do not test generated internals or use live accounts by default.

Run focused existing tests first. Use `npm run check` for completion/pre-PR and
record which stage failed. `api-client` currently has lint only; do not run invented
`api-client:test` or `api-client:build` targets. An application build checks its use.
`npm test` currently targets only `wishmate`, not every future Nx library.
No E2E target was found; label manual checks as manual, and never claim E2E passed.

These Markdown files themselves do not require rebuilding the application. Validate
their structure, paths and consistency. For code changes do not skip required gates
silently; unavailable dependencies/backend mean blocked or unrun, not passed.
