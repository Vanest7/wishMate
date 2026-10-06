# WishMate frontend instructions

## Scope and evidence

This configuration lives at `<repository-root>/.claude/`. The Angular workspace is
`wishmate-frontend/`; the Spring backend is `demo/`. Resolve these paths from the
repository root, regardless of the terminal's current directory.

Build Angular features for gift lists shared within groups and events. Keep backend
changes limited to explicitly requested work. Read backend code to verify contracts.
Reply in the user's language. Write code, comments, identifiers, test names and commit
messages in English. UI text may be Spanish; do not translate identifiers.

Baseline reviewed: `develop`, commit `152e88777349a713dce133c683810e12cd4f302c`,
2026-10-06. This is a snapshot, not a guarantee about later commits.
Read `references/project-baseline.md` in this directory before assuming a capability.
Actual controller -> use case -> service behavior takes precedence over old examples.
If desired behavior conflicts with existing code, report the discrepancy; do not
silently change permissions, privacy, or the API to reconcile it.

## Mandatory working sequence

1. Inspect the relevant files, current git diff, and applicable project instructions.
2. Read `rules/business-domain.md` for any business feature. Check the corresponding
   controller, request/response DTOs and invoked use cases before implementing it.
3. Follow `rules/angular-architecture.md`, `rules/angular-conventions.md`,
   `rules/api-and-auth.md` and `rules/testing.md` for frontend changes.
4. Describe a short implementation plan for multi-file work, then implement it.
5. Verify the changed behavior, inspect the final diff, and report actual results.
   Distinguish passed checks, failures, skipped checks and missing infrastructure.

Read referenced files explicitly when they have not been loaded. Do not assume that
a reference link or nested rule was automatically loaded into a subagent's context.

## Existing stack and commands

The reviewed package manifest declares Angular/Material 22.1, Nx 23.2.1,
RxJS 7.8, Tailwind 4, Vitest and ng-openapi-gen 1.1.0. Recheck the manifest and
lockfile before using version-specific APIs. Do not copy the old monorepo's
PrimeNG, Formly, translation services, Karma flags or infrastructure.

Run npm/Nx commands from `wishmate-frontend/`:

| Task | Existing command |
| --- | --- |
| Development server | `npm start` |
| Production build | `npm run build` |
| Lint | `npm run lint` |
| Formatting check | `npm run format:check` |
| Application tests, no watch | `npm test` |
| Full existing gate | `npm run check` |
| Generate API client from checked-in schema | `npm run api:generate` |

Use `/new-feature`, `/api-client-sync`, `/business-review` and `/pre-pr` for their
specific workflows. Review agents are optional read-only specialists, not an
automatic requirement for every edit.

## Non-negotiable safeguards

- Never reveal a wish's reservation existence or reserver to its owner.
- Never invent an endpoint, model property, group permission or event transition.
- Never edit `libs/api-client/src/lib/generated/` manually.
- Never disable auth, CSRF, lint rules or tests merely to make a task pass.
- Do not introduce dependencies, global stores or generic CRUD frameworks without
  a concrete need. Prefer the installed stack and simple feature-local code.
- Preserve unrelated changes. Do not commit, push, publish, deploy or send messages
  unless the user requests that action. Do not write credentials into tracked files.
- Documentation describes conventions; it does not enforce ESLint, permissions or
  backend security. Report separately when technical enforcement is missing.
