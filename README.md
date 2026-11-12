# WishMate

WishMate is a personal full-stack project built with Angular, Java, Spring Boot, and MariaDB. It helps friends and families organize gift-giving through private groups, shared wish lists, and events. Members can reserve gifts to avoid duplicate purchases. Wish owners cannot see whether their gifts have been reserved.

## Live demo

[Open WishMate on Netlify](https://wishmateve.netlify.app/auth/login)

Choose **Crear cuenta** on the sign-in page to try it. The frontend runs on Netlify and sends API requests to Spring Boot on Render. The hosted backend or database may need time to wake up after inactivity. If the first request fails, give the service a moment and try again.

## Run locally with Docker

Install Python 3, OpenSSL, and Docker with the Compose v2 plugin. Start Docker Desktop or the Docker Engine. The first run needs internet access to download images and dependencies. Java, Node.js, and MariaDB run inside the containers.

From the repository root, next to `wishmate-schema.sql`, run:

```bash
python3 run_docker.py
```

Open [http://localhost:8080/auth/login](http://localhost:8080/auth/login) once the script prints that WishMate is ready. The first build can take several minutes. If port 8080 is already in use, run `python3 run_docker.py up --port 8081` and open [http://localhost:8081/auth/login](http://localhost:8081/auth/login).

| Command | What it does |
| --- | --- |
| `python3 run_docker.py up` | Builds the current checkout, starts the containers and waits for the frontend and API. |
| `python3 run_docker.py logs` | Follows container logs. Press Ctrl+C to exit the log view. |
| `python3 run_docker.py down` | Removes the containers and keeps the local database volume. |

The script creates database passwords and RSA signing keys on the first run and keeps them under `~/.wishmate-docker/` for that checkout. MariaDB imports `wishmate-schema.sql` and adds the user handle column when it initializes a fresh volume. Nginx serves Angular and forwards `/api/` to Spring Boot on the same origin. The web port is bound to the local machine.

### Try the gift reservation flow

1. Create an account, a group, and an event.
2. Add a wish to the event.
3. In a private browser window, create a second account. Use its username to add it to the group from the first account.
4. From the second account, open the event and reserve the wish. Switch back to the first account to check that the reservation stays hidden from the wish owner.

## Technology stack

| Component | Project configuration |
| --- | --- |
| Frontend | Angular 22.1, Nx, Angular Material, Tailwind CSS, RxJS and Vitest |
| API client | OpenAPI client generated with ng-openapi-gen |
| Java | 21 |
| Spring Boot | 3.5.4 |
| Persistence | Spring Data JPA / Hibernate |
| Database | MariaDB; local verification used 10.11.13 |
| Security | Spring Security, RSA-signed JWT (RS256), CSRF cookies |
| Mapping | MapStruct 1.6.3 and Lombok |
| Validation | Jakarta Validation |
| API documentation | Springdoc OpenAPI 2.8.9 / Swagger UI |
| Tests | JUnit Jupiter, Mockito, MockMvc, Spring Security Test |
| Build | Maven Wrapper; optional npm command shortcuts |

## Project layout

| Path | Purpose |
| --- | --- |
| `demo/pom.xml` | Backend dependencies and build plugins |
| `demo/mvnw` | Maven Wrapper for Linux/macOS |
| `demo/src/main/java/com/gifts/demo/` | Backend source code |
| `demo/src/main/resources/application.properties` | Shared configuration |
| `demo/src/main/resources/application-dev.properties` | Development overrides |
| `demo/src/test/java/` | Automated tests |
| `wishmate-frontend/` | Angular/Nx workspace and generated API client |
| `wishmate-schema.sql` | MariaDB schema export for an empty database |
| `run_docker.py` and `compose.wishmate.yaml` | Local container startup and service configuration |
| `package.json` | npm shortcuts for Maven commands |

Controllers handle HTTP requests and map API DTOs. Domain use cases and services enforce business rules through domain repository interfaces. Persistence implementations adapt those interfaces to the existing DAO/JPA infrastructure.

The Angular app is organized by feature. Pages coordinate data access and UI components. Shared authentication and HTTP handling live under `core/`, and the generated transport client is exposed by the `@wishmate/api-client` Nx library. See the [frontend architecture notes](wishmate-frontend/docs/frontend-architecture.md) for component boundaries and data access.

The [GitHub Actions workflow](.github/workflows/ci.yml) runs frontend and backend checks for `develop` and uploads build artifacts.

## Manual backend setup

Use the following steps to run Spring Boot directly for development, including Swagger, API tests and CORS checks. If the Docker setup is running on port 8080, stop it or use a different port before starting the backend directly.

### Prerequisites

- JDK 21 available to both IntelliJ and the terminal.
- A running MariaDB server and the MariaDB command-line client.
- OpenSSL to generate local RSA keys when no key pair exists.
- Node.js and npm only if using the `package.json` shortcuts.
- Network access for the first Maven Wrapper execution and dependency downloads.

Check the tools you intend to use:

```bash
java -version
mariadb --version
openssl version
node --version
npm --version
```

A global Maven installation is not required. The npm scripts use Bash and are intended for the Ubuntu development environment. No `npm install` is required for the command-only package.json.

### 1. Prepare MariaDB

#### Existing development database

If `regalos_db` already contains the current schema, use that database and skip initialization. Do not import the schema over existing application tables.

#### New local installation

The following steps are for a new, empty database. Run the commands from the repository root. On Ubuntu installations with socket-authenticated administration, open an administrative session with:

```bash
sudo mariadb
```

If your installation uses a different administrative login, use that instead. Create the database:

```sql
CREATE DATABASE regalos_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;
```

Exit the SQL client, then import the current schema using an administrative account:

```bash
sudo mariadb regalos_db < wishmate-schema.sql
sudo mariadb regalos_db < docker/02-user-handle.sql
```

The second command adds the unique public handle column used by account registration. For an existing database, check the schema before applying the [user handle migration](demo/USER_HANDLE_MIGRATION.sql).

The current export defines these tables:

- `usuarios`
- `grupos`
- `grupo_usuarios`
- `eventos`
- `deseos`
- `reservas_regalos`
- `tokens`

The schema file contains table definitions, indexes and constraints, without application rows or `DROP TABLE` statements. It does not select or create the database, so the import command supplies `regalos_db` explicitly. Use a MariaDB client compatible with the export; it includes MariaDB-specific dump directives.

Create a local application account if you do not already have one. Run the following as a database administrator and replace the password placeholder before executing:

```sql
CREATE USER 'wishmate_app'@'localhost'
    IDENTIFIED BY 'REPLACE_WITH_A_LOCAL_PASSWORD';

GRANT SELECT, INSERT, UPDATE, DELETE
    ON regalos_db.*
    TO 'wishmate_app'@'localhost';
```

Configure the actual account credentials through environment variables, not through committed configuration files.

Hibernate is configured with:

```properties
spring.jpa.hibernate.ddl-auto=none
```

The application does not create or migrate the schema automatically. Future schema changes must be applied explicitly.

### 2. Prepare JWT keys

The default key locations are:

- `${user.home}/.wishmate/jwt-private.pem`
- `${user.home}/.wishmate/jwt-public.pem`

Keep an existing working pair. For a fresh setup only, generate a PKCS#8 private key and its corresponding public key:

```bash
mkdir -p "$HOME/.wishmate"
chmod 700 "$HOME/.wishmate"

if [ -e "$HOME/.wishmate/jwt-private.pem" ] || [ -e "$HOME/.wishmate/jwt-public.pem" ]; then
    printf '%s\n' 'Existing key files found. Keep or inspect the existing pair.'
else
    (
        umask 077
        openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 \
            -out "$HOME/.wishmate/jwt-private.pem" &&
        openssl pkey -in "$HOME/.wishmate/jwt-private.pem" -pubout \
            -out "$HOME/.wishmate/jwt-public.pem"
    )
fi
```

Both keys must belong to the same pair. The current implementation loads an unencrypted PEM private key; protect it with filesystem permissions and keep it outside the repository. Replacing the key pair invalidates previously issued access tokens.

### 3. Configure environment variables

| Variable | Required / default | Purpose |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | Set to `dev` for local development | Loads development overrides |
| `DB_URL` | Development fallback: `jdbc:mariadb://localhost:3306/regalos_db` | JDBC database URL |
| `DB_USERNAME` | Required | MariaDB application user |
| `DB_PASSWORD` | Required | MariaDB application password |
| `JWT_PUBLIC_KEY_LOCATION` | Optional | Defaults to `file:${user.home}/.wishmate/jwt-public.pem` |
| `JWT_PRIVATE_KEY_LOCATION` | Optional | Defaults to `file:${user.home}/.wishmate/jwt-private.pem` |
| `CORS_ALLOWED_ORIGINS` | Development fallback: `http://localhost:4200` | Comma-separated browser origins |

Outside the `dev` profile, `DB_URL` has no fallback and CORS has no allowed origins unless configured. The configured JWT issuer is `wishmate`; access-token lifetime is `PT15M` (15 minutes).

**The database URL must start with `jdbc:`.** `mariadb://localhost:3306/regalos_db` is not a valid JDBC URL for this configuration. An explicitly defined `DB_URL` overrides the development fallback.

#### IntelliJ IDEA

In **Run → Edit Configurations**, add the variables to the configuration you actually execute:

- Java/Spring Boot configuration for `GiftsApplication`.
- JUnit configuration for the complete test suite.
- npm configuration for a package.json script.

These are independent configurations. Variables set in one are not automatically copied to the others or to the integrated terminal. Enter values without surrounding quote characters in the environment-variable table.

For custom key locations, use full resource paths, for example `file:/absolute/path/jwt-private.pem`. Avoid storing credentials in shared run configurations committed to Git.

#### Bash terminal

Set the variables in the terminal where you will run Maven or npm:

```bash
export SPRING_PROFILES_ACTIVE=dev
export DB_URL='jdbc:mariadb://localhost:3306/regalos_db'

read -r -p 'Database username: ' DB_USERNAME
export DB_USERNAME

read -r -s -p 'Database password: ' DB_PASSWORD
printf '\n'
export DB_PASSWORD
```

The password prompt does not echo the password. These exports apply to the current shell and its child processes.

The adopted setup uses environment variables. It does **not** automatically load a `.env` file. Do not add credentials or private keys to Git.

### 4. Run the backend

#### Using npm

From the repository root, with the environment configured:

```bash
npm start
```

The scripts supplied for development activate the `dev` profile. When using an IntelliJ npm run configuration, set its environment variables as described above.

#### Using Maven directly

From the repository root, enter the backend directory:

```bash
cd demo
bash ./mvnw spring-boot:run
```

This direct command requires `SPRING_PROFILES_ACTIVE=dev` in the environment for development defaults.

#### Using IntelliJ

Run `com.gifts.demo.GiftsApplication` using JDK 21 and the configured environment.

The default HTTP address is [http://localhost:8080](http://localhost:8080). Look for `Started GiftsApplication` in the log, then execute an authenticated read to confirm database access. A startup message alone does not exercise every API path.

### 5. Build and run tests

Run npm commands from the repository root:

| Command | Action |
| --- | --- |
| `npm start` | Start the backend |
| `npm test` | Run all tests |
| `npm run test:clean` | Clean compiled output and run all tests |
| `npm run test:controllers` | Run classes matching `*ControllerTest` |
| `npm test -- -Dtest=AuthControllerTest` | Run one test class |
| `npm run compile` | Compile main source code |
| `npm run build` | Clean, run tests and package the application |
| `npm run clean` | Remove Maven build output |

Equivalent Maven commands, executed from `demo/`, include:

```bash
bash ./mvnw test
bash ./mvnw clean test
bash ./mvnw test -Dtest=AuthControllerTest
bash ./mvnw clean package
```

`GiftsApplicationTests` checks that the application entry-point class is loadable. Other tests exercise focused behavior with mocks and MVC slices. Use the startup and end-to-end steps in this guide to verify the API against a running MariaDB database.

Test reports are generated under `demo/target/surefire-reports/`.

The POM includes `spring-boot-maven-plugin`. After a successful package build, the current artifact coordinates produce:

```bash
java -jar demo/target/demo-0.0.1-SNAPSHOT.jar
```

Run this from the repository root with the same environment variables. Stop any existing backend first to avoid a port conflict. This command is a packaging workflow; do not assume it has been validated merely because IDE startup works.

### 6. Use Swagger and authentication

- [Swagger UI](http://localhost:8080/swagger-ui/index.html)
- [OpenAPI JSON](http://localhost:8080/v3/api-docs)

#### Authenticate in Swagger

1. Execute `GET /api/auth/csrf` from Swagger. The response contains `headerName` and `token`; the browser also receives the matching `XSRF-TOKEN` cookie.
2. Open **Authorize** and paste the returned JSON token into `csrfToken`.
3. Execute `POST /api/auth/login` with an existing user's credentials:

```json
{
  "email": "your-email@example.test",
  "password": "your-password"
}
```

4. Copy the returned `accessToken` into `bearerAuth`. Paste only the token; Swagger adds the `Bearer` prefix.
5. Execute `GET /api/groups` to confirm authenticated access and a database read.

Use the JSON token returned by the CSRF endpoint, not a token copied from an unrelated Postman session. The CSRF header and cookie must come from the same client flow. A CSRF token obtained again may have a different encoded representation; do not compare the JSON token and raw cookie for literal equality.

| Request | Bearer token | CSRF header and cookie |
| --- | --- | --- |
| `GET /api/auth/csrf` | Not required | Not required |
| `POST /api/auth/register` | Not required | Exempt |
| `POST /api/auth/login` | Not required | Required |
| Protected reads | Required | Not required |
| Protected writes | Required | Required |

Login and CSRF responses use `Cache-Control: no-store`. If an access token expires, log in again and replace it in Swagger. Refresh/logout endpoints are not exposed by the reviewed AuthController.

#### Response documentation

Swagger lists the current operations and their documented responses. Success responses use the existing response DTOs; list endpoints return arrays, and `204` responses have no body. `Location` is documented only where a controller sets it.

Application errors handled by `ApiExceptionHandler` use this shape:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "fieldErrors": {
    "name": "Group name is required"
  }
}
```

This example applies to an empty group name. Exact codes and messages depend on the failure. Spring Security can reject requests before the controller, so its `401` and `403` responses are not guaranteed to use this JSON schema.

Invalid event states currently produce `400`. Duplicate reservations produce `409`. Permission checks can run before resource lookups, so a missing resource may be preceded by a `403` access denial.

`springdoc.override-with-generic-response=false` keeps the explicitly documented operation responses instead of adding every global handler response to every endpoint.

### API overview

Replace path placeholders with the IDs returned by earlier requests.

| Area | Operations |
| --- | --- |
| Authentication | `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/csrf` |
| Users | `GET /api/users/search` |
| Groups | `POST` and `GET /api/groups`; `GET`, `PATCH`, `DELETE /api/groups/{groupId}` |
| Memberships | `GET` and `POST /api/groups/{groupId}/members`; `PATCH` and `DELETE /api/groups/{groupId}/members/{userId}`; `DELETE /api/groups/{groupId}/members/me` |
| Events | `POST` and `GET /api/groups/{groupId}/events`; `GET` and `PUT /api/groups/{groupId}/events/{eventId}` |
| Event lifecycle | `POST /api/groups/{groupId}/events/{eventId}/activate`, `/close`, `/archive` |
| Wishes | `POST` and `GET /api/groups/{groupId}/events/{eventId}/wishes`; `GET`, `PUT`, `DELETE /api/groups/{groupId}/events/{eventId}/wishes/{wishId}` |
| Wish reservation status | `GET /api/groups/{groupId}/events/{eventId}/wishes/{wishId}/reservation-status` |
| Member wishes | `GET /api/groups/{groupId}/events/{eventId}/wishes/user/{userId}` |
| Reservations | `POST` and `DELETE /api/reservations/groups/{groupId}/events/{eventId}/wishes/{wishId}` |
| Own reservations | `GET /api/reservations`, `GET /api/reservations/{reservationId}` |

The reservation creation response currently uses the creation request URI as `Location`. Read a reservation using `GET /api/reservations/{reservationId}` and the returned reservation ID.

### 7. CORS verification

Development allows `http://localhost:4200` by default. Allowed origins can be overridden through `CORS_ALLOWED_ORIGINS`. The configuration permits credentials and the `Authorization`, `Content-Type` and `X-XSRF-TOKEN` request headers.

Check a preflight request:

```bash
curl -i -X OPTIONS 'http://localhost:8080/api/groups' \
  -H 'Origin: http://localhost:4200' \
  -H 'Access-Control-Request-Method: POST' \
  -H 'Access-Control-Request-Headers: authorization,content-type,x-xsrf-token'
```

With the default development origin, expect `200` and:

```http
Access-Control-Allow-Origin: http://localhost:4200
Access-Control-Allow-Credentials: true
```

Repeat with `Origin: http://localhost:4300`; expect `403` unless that origin was explicitly allowed. These checks validate backend preflight handling, not the complete Angular integration. The frontend must also send the required credentials and CSRF header on relevant requests.

### 8. End-to-end verification

Use disposable data and keep the two users in separate browser sessions. Authenticate both through the Swagger flow above.

1. As user A, create a group using `POST /api/groups` with `{"name":"E2E verification"}`. Expect `201` and save `groupId`.
2. Create an event within that group using `{"name":"E2E event","description":"Backend verification","status":"ACTIVE"}`. Expect `201` and save `eventId`.
3. Create a wish using `{"name":"E2E book","description":"Verification wish","estimatedPrice":20.00}`. Expect `201` and save `wishId`.
4. Read the group and wish through their GET endpoints. Expect `200` and matching values.
5. Register user B if needed, save the returned user ID, then add B to the group as A through the memberships POST endpoint with `userId` set to B's actual ID. Expect `201`.
6. As B, reserve A's wish using the reservation POST endpoint without a body. Expect `201`, the matching `wishId`, a reservation ID and a non-null `reservedAt`.
7. Read B's reservations. Expect `200` and the new reservation. Repeating the reservation POST should return `409`.
8. As A, read B's reservation by ID. Expect `403`; the wish owner must not receive the other user's reservation details.
9. Restart the backend, authenticate again if necessary, then retrieve the group, wish and B's reservation. Expect `200` and persisted values.
10. As B, cancel the reservation; as A, delete only the test group. Expect `204` for both. Any test user account created for this flow remains in the database.

This backend flow was reported as successful in local development during SCRUM-23. To verify the same behavior through Angular, follow [the gift reservation flow](#try-the-gift-reservation-flow) in the Docker section.

## Troubleshooting

| Symptom | Check |
| --- | --- |
| `'url' must start with "jdbc"` | `DB_URL` must begin with `jdbc:mariadb://`. Remove literal surrounding quotes in IntelliJ. Check whether `SPRING_DATASOURCE_URL` overrides it. |
| Works in the IDE but fails through npm | Set variables in the npm run configuration or export them in the invoking terminal. |
| Missing datasource variables | Check the actual run configuration and whether `dev` is active. |
| `Access denied` from MariaDB | Check the username, password and database privileges. |
| Connection refused | Check the MariaDB service, hostname and port. |
| Missing or incompatible JWT keys | Check the `file:` resource locations, file permissions, PEM formats and matching RSA key pair. |
| Port 8080 is already in use | Stop the other backend process before starting another instance. |
| `401` from a protected endpoint | Log in again and authorize the current JWT. |
| `403` on a write | Check CSRF header/cookie pairing and the operation's membership/ownership rules. |
| CSRF test passes alone but returns `X-CSRF-TOKEN` in the suite | `.with(csrf())` can alter the shared test repository. The real-cookie contract test uses `@DirtiesContext(methodMode = BEFORE_METHOD)` and checks `XSRF-TOKEN`. |
| Spring Boot fails on startup | Read the deepest `Caused by` entry and verify the datasource settings and JWT key files. |
| Duplicate `corsConfig` bean | Keep the configuration in `common.cors.CorsConfig`; do not add a second class with the same bean name. |

### Known schema/application difference

The exported `deseos.descripcion` column permits null and up to 500 characters, while current create/update validation and the domain service require nonblank content with a maximum of 255 characters. The API applies the stricter rule. No column reduction or data migration was performed as part of this configuration task.

## Reference documentation

- [Spring Boot 3.5 configuration](https://docs.spring.io/spring-boot/3.5/how-to/properties-and-configuration.html)
- [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/3.5/maven-plugin/run.html)
- [Spring Security CORS](https://docs.spring.io/spring-security/reference/servlet/integrations/cors.html)
- [Springdoc OpenAPI v2](https://springdoc.org/v2/)
- [MariaDB schema export](https://mariadb.com/docs/server/clients-and-utilities/backup-restore-and-import-clients/mariadb-dump)
- [OpenSSL key generation](https://docs.openssl.org/3.0/man1/openssl-genpkey/)
