# Reviewed project baseline

Repository: `Vanest7/wishMate`.
Branch inspected: `develop`.
Commit: `152e88777349a713dce133c683810e12cd4f302c`.
Review date: 2026-10-06.

Use this as an evidence record, not as permanently current configuration. Recheck
changed sources before working. The shipped rules include NEW frontend conventions;
they do not assert that all proposed folders, services or screens already exist.

## Verified configuration

| Evidence | Observation |
| --- | --- |
| `wishmate-frontend/package.json` | Angular/Material ~22.1.0, Nx 23.2.1, RxJS ~7.8.0, Vitest ^4.1.11, Tailwind ^4.3.3, ng-openapi-gen 1.1.0 |
| `wishmate-frontend/apps/wishmate/project.json` | Angular application build; unit-test executor uses Vitest; app selector prefix is app |
| `wishmate-frontend/apps/wishmate/src/app/app.ts` | Standalone-style shell importing RouterOutlet and Material button |
| `wishmate-frontend/apps/wishmate/src/app/app.routes.ts` | Empty route array at this revision |
| `wishmate-frontend/apps/wishmate/src/app/app.config.ts` | HttpClient, router and generated API config for localhost:8080; no auth interceptor registered |
| `wishmate-frontend/ng-openapi-gen.json` | Checked-in schema input, Observable services, no NgModule, generated index |
| `wishmate-frontend/libs/api-client/src/index.ts` | Re-exports generated index |
| `wishmate-frontend/libs/api-client/project.json` | Only lint target; tags scope:shared and type:data-access |
| `wishmate-frontend/nx.json` | No defaultBase; library generator defaults to no unit test runner |
| `wishmate-frontend/tsconfig.base.json` | Only verified custom alias is @wishmate/api-client; strict false here |
| `wishmate-frontend/eslint.config.mjs` | Flat config and module boundaries; wildcard dependency constraints |
| `wishmate-frontend/package.json` prepare | Husky accounts for the frontend subdirectory |

No PrimeNG, Formly, Transloco or Jest dependency is declared in this frontend manifest.
Do not inherit them from the old company's monorepo. The source tree has no tracked
AGENTS.md or CLAUDE.md at this revision; preserve any local/newer instructions instead
of assuming their absence forever.

## Contract gaps that matter to frontend planning

1. No UserController/current-user/profile endpoint exists in the reviewed tree/schema.
2. AuthController exposes register, csrf and login only. Refresh-token domain files
   alone do not establish an implemented refresh or server logout endpoint.
3. GiftReservationServiceImpl.getVisibleStatus exists internally, but no reviewed
   controller exposes it; WishResponse has no reservation-status field. This blocks
   a reliable pre-reservation availability badge until the contract is extended.
4. The reservation list is GET /api/reservations, not /api/reservations/me.
5. Group rename is PATCH /api/groups/{groupId}, not a /name suffix.
6. Wish/event updates use PUT. There is no DELETE event endpoint or reopen action.
7. Adding members uses an existing numeric user ID; an invitation or user-search
   workflow is not part of the current contract.
8. Reservation lists can still include reservations after group membership changes:
   reads filter by reserver, but cancellation rechecks membership. Cleanup/transfer
   behavior needs a deliberate product/backend decision, not a frontend guess.

## Business evidence map

Backend paths below are relative to `demo/src/main/java/com/gifts/demo/`.

| Concern | Sources inspected |
| --- | --- |
| Active group permission path | controller/GroupController.java, controller/GroupMemberController.java, domain/usecase/group/impl/* |
| Roles and owner protection | domain/service/impl/GroupPermissionServiceImpl.java, GroupAccessServiceImpl.java |
| Event permissions and transitions | controller/EventController.java, domain/usecase/event/impl/*, domain/service/impl/EventServiceImpl.java, EventStateServiceImpl.java |
| Wish ownership and validation | controller/WishController.java, controller/webmodel/wish/*, domain/usecase/wish/impl/*, domain/service/impl/WishServiceImpl.java |
| Reservation privacy and cancellation | controller/GiftReservationController.java, domain/usecase/reservation/impl/*, domain/service/impl/GiftReservationServiceImpl.java |
| Returned wish information | controller/mapper/WishWebMapper.java, controller/webmodel/wish/WishResponse.java |
| Auth, CSRF and identity | controller/AuthController.java, controller/webmodel/auth/*, security/SecurityConfig.java, security/JwtService.java |
| Errors | controller/exception/ApiExceptionHandler.java |

Relevant backend test files were also inspected for their documented scenarios:
`GiftReservationServiceImplTest`, `EventStateServiceImplTest`, and
`GroupMembershipUseCasesTest` under `demo/src/test/java/com/gifts/demo/`.
No application build, frontend tests or backend tests were executed to produce this
instruction package. This package changes documentation only.

## Claude format references

The package uses project-local `.claude/CLAUDE.md`, rules, skills and agents as
documented by Claude Code. It does not install global skills or configure permissions.

- https://code.claude.com/docs/en/memory
- https://code.claude.com/docs/en/skills
- https://code.claude.com/docs/en/sub-agents
