---
paths:
  - "wishmate-frontend/**/*"
---
# API, authentication and errors

## Generated client

`openapi/wishmate.json` is the checked-in input. `ng-openapi-gen.json` generates
Observable-based services into `libs/api-client/src/lib/generated/`; the public
barrel is `libs/api-client/src/index.ts`. Run `npm run api:generate` from the frontend.
This command does NOT fetch a fresh schema from a running backend.

Check the real operation signature before using it. Do not manually duplicate DTOs,
paths or transport methods. Preserve optional/null fields; validate response data
before using optional generated IDs to build routes. Do not patch generated `any`
or optional declarations by hand: fix the source contract and regenerate when in scope.

Use `references/api-contract.md` as a snapshot index and inspect the source schema
and controller. If an operation is missing, state the gap rather than fabricate a call.

## Current authentication contract

- Register accepts userName/email/password and is exempt from CSRF.
- GET `/api/auth/csrf` returns `{ headerName, token }` and the browser must retain
  its matching cookie. It takes no `csrfToken` query parameter.
- POST `/api/auth/login` takes email/password and requires the CSRF header/cookie.
  It returns accessToken/tokenType/expiresIn. Registration does not return a login token.
- Authenticated calls use Bearer JWT. Unsafe authenticated calls also require CSRF;
  Bearer authentication does not exempt them in the current SecurityConfig.
- The current cookie repository uses its default HttpOnly configuration. Consume the
  JSON CSRF token; do not assume JavaScript can read the cookie or disable HttpOnly.
- Do not assume Angular's default XSRF handling will cover an absolute cross-origin
  API URL. Implement and verify a narrowly API-scoped interceptor with credentials
  where necessary, plus the returned header/token. Never send tokens to third parties.
- Coordinate CSRF acquisition before login/writes. Do not endlessly retry a 403 or
  automatically repeat a potentially completed mutation.
- Refresh and server logout endpoints are absent. Do not implement calls to them.
  New client session code should keep tokens in memory by default, handle expiry
  and clear state on local logout. Persistent sessions require an explicit design.
- There is no current-user/profile endpoint in the reviewed tree/schema. The JWT
  subject is the user ID in `security/JwtService.java`. Decoding it may identify the
  current UI user but does not verify the token or grant authorization. Do not invent
  name/email/group-role claims. Derive group roles from membership responses.
- Route guards and hidden actions are UX checks; the backend remains authoritative.
- `app.config.ts` currently sets the API base to localhost:8080. Move configurable
  origins into core config when implementing deployment support; do not claim this
  package already changed runtime configuration or configured a proxy/CORS.

Read `demo/src/main/java/com/gifts/demo/security/SecurityConfig.java` and
`controller/AuthController.java` from the repository root before changing this flow.

## Error behavior

Application errors use `{ code, message, fieldErrors }`. Security-filter failures
can have empty/different bodies. Treat bodies as unknown and narrow their shape.

| Response | UI handling |
| --- | --- |
| 400 | Field/domain validation; preserve entered values |
| 401 | Differentiate failed login from expired/missing session; avoid redirect loops |
| 403 | Permission or CSRF issue; do not label every failure as expired authentication |
| 404 | Missing resource; do not leak unrelated resource information |
| 409 | Conflict, including concurrent reservation or membership changes; reconcile state |
| 500 / network failure | Safe generic feedback and intentional retry for safe reads |

Map known field errors inline and give safe fallback feedback for unknown errors.
Avoid duplicate global and page notifications. Do not log credentials, full auth
responses or private reservation payloads. A null/unknown reservation status is never
an available reservation. Clear session-scoped data before displaying another account.
