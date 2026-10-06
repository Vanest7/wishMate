---
name: pre-pr
description: Validate WishMate frontend changes before a pull request using existing formatting, lint, Vitest and build commands. Use when checking readiness or completing a frontend change.
---

# Validate before a pull request

1. Read `.claude/rules/testing.md` and current `wishmate-frontend/package.json`.
   Inspect branch, diff and status from the Git root; preserve unrelated modifications.
2. Run `npm run check` from `wishmate-frontend/`. The current order is formatting,
   lint, application tests without watch, then production build; it stops on failure.
3. Diagnose the first failure. Fix formatting/lint only within the intended change;
   `npm run format` and `lint:fix` may affect the whole workspace, so prefer targeted
   fixes and review their diff. Rerun after an actual fix; do not loop on the same blocker.
4. Resolve test/build defects without disabling checks or removing assertions. Report
   pre-existing failures separately. Missing dependencies/services are blockers, not passes.
5. Confirm any new libraries' tests were actually included. `npm test` only runs the
   application target at baseline. Do not add Karma browser flags to Vitest commands.
6. For a faster intermediate affected check, first inspect actual Nx targets and
   determine the comparison base from the intended PR. Do not assume `defaultBase`
   is configured: it is absent in the reviewed nx.json. Resolve the Git merge base
   and account for uncommitted changes; if uncertain, use the existing full gate.
7. Review business/privacy changes against `.claude/rules/business-domain.md` and
   the current contract. Verify no generated files were manually edited or credentials added.
8. Return a short table of check/result and any failing file/error. State skipped
   stages and manual-only checks. Do not open a PR, commit or push unless requested.

For instruction-only changes, validate Markdown/frontmatter/references and business
consistency; do not claim an application test run is necessary or already completed.
