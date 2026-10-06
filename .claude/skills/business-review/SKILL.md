---
name: business-review
description: Review WishMate functionality against implemented business rules, permissions, event transitions and reservation privacy. Use before implementing a business flow or auditing its correctness.
---

# Audit business behavior

Scope: $ARGUMENTS

1. Read `.claude/rules/business-domain.md`, `.claude/rules/api-and-auth.md`,
   `.claude/references/project-baseline.md` and `.claude/references/api-contract.md`.
2. Follow actual controller -> use case -> service calls for the requested actions.
   Compare the request/response contract and matching tests. Do not confuse test
   inspection with test execution, or an internal service method with an endpoint.
3. Build the relevant role/ownership/state cases, including invalid parent IDs,
   nonmembership, stale state, concurrent reservation and account switching.
4. Inspect visible text, disabled controls, counts and accessibility output for
   reservation leaks. Check that absent reservation status remains unknown.
5. Distinguish implemented behavior, confirmed product intent and missing decisions.
   Current event management is member-wide; do not silently narrow it to managers.
   Inspect cleanup before assuming leaving a group deletes wishes/reservations.
6. Report an action/actor/state/expected/observed/evidence table and prioritized
   findings. Explain the minimal fix and whether it requires backend work. This
   workflow is read-only unless the user has also requested implementation.
