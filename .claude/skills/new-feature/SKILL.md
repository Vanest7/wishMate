---
name: new-feature
description: Implement a WishMate Angular feature, page, form or CRUD flow using the current API and business rules. Use when adding groups, events, wishes, reservations or authentication screens.
---

# Implement a frontend feature

Input: $ARGUMENTS

1. Locate the repository root and frontend workspace. Read `.claude/CLAUDE.md`,
   `.claude/rules/business-domain.md`, `.claude/rules/angular-architecture.md`,
   `.claude/rules/angular-conventions.md`, `.claude/rules/api-and-auth.md` and
   `.claude/rules/testing.md`. Inspect current source, manifests and user changes.
2. Define the requested user flow and its acceptance cases: actor/group role, event
   state, resource ownership, loading/empty/error states and successful navigation.
   Infer routine layout choices from these conventions; ask only about blocking
   product choices, never invent backend behavior to fill a gap.
3. Verify each required operation in the schema, generated service and invoked backend
   implementation. Check `.claude/references/api-contract.md` for known gaps. If an
   operation is absent, complete independent work and clearly identify the blocked
   part; do not silently add a backend feature or an imaginary client method.
4. Implement in `apps/wishmate/src/app/features/<feature>/` following the documented
   layout. Create only needed folders. Reuse generated request/response types.
   Keep shared UI neutral and feature state scoped to the feature/route as appropriate.
5. Add lazy routes using actual parent parameters. Reuse a typed form between create
   and edit where appropriate; explicitly map values and implement server validation.
   Centralize nontrivial permission predicates in pure feature-local helpers.
6. Protect surprise privacy, session isolation and duplicate writes. Use Material and
   the installed styling approach. Do not invent i18n, permission or table frameworks.
7. Add relevant behavioral Vitest tests. Verify with existing targets and the pre-PR
   workflow. If new libraries are necessary, wire test discovery deliberately.
8. Report changes, actual checks and remaining API/product gaps. Do not commit/push
   as a side effect of feature creation.
