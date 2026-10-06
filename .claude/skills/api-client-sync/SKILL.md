---
name: api-client-sync
description: Synchronize WishMate's checked-in OpenAPI contract and ng-openapi-gen Angular client after an API change, or diagnose missing generated methods and DTO mismatches.
---

# Synchronize the API client

1. Read `.claude/rules/api-and-auth.md` and `.claude/references/api-contract.md`.
   Inspect `wishmate-frontend/package.json`, `ng-openapi-gen.json`, the schema,
   public barrel and current diff. Preserve user edits and report manual generated
   edits before regeneration would erase them.
2. Determine whether the request is only regeneration from the checked-in schema
   or refreshing the schema from the matching backend. Do not claim these are the same.
3. For a refresh, identify the authorized backend URL and version; use `/v3/api-docs`
   from that backend. Fetch into a temporary file and parse it as JSON; require an
   OpenAPI version plus paths/components as appropriate. Reject login HTML/error bodies.
   Review changed operations before replacing `openapi/wishmate.json`.
   Do not run a backend with guessed database credentials or contact production blindly.
4. Run `npm run api:generate` from `wishmate-frontend/` using locked dependencies.
   Preserve `services: true`, `promises: false`, `module: false`, `indexFile: true`
   unless the task explicitly changes the generator design.
5. Review diff for changed request requirements, response types, enum values and
   operation IDs. Check that `getCsrfToken` takes no CSRF query input and the public
   barrel still exports `./lib/generated`. Never repair generator output by hand.
6. Update handwritten callers through the public alias. Keep local view models
   separate; do not weaken types to hide a contract mismatch.
7. Run lint, affected behavioral tests and application build; use the full check gate
   for completion. The API library has no independent test/build target at baseline.
8. State schema origin/revision, changed operations, caller changes and actual checks.
   If backend and schema disagree, report which source was inspected and what remains
   unresolved. Do not advertise unexposed service methods as generated endpoints.
