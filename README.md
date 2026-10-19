# Spring Boot Backend Template

A production-minded starting point for Spring Boot REST backends: JWT authentication, user management,
standard error responses, tests against a real database, and one-command scripts for Linux, macOS and Windows.

Clone it, run `scripts/rename-project.sh`, delete what you don't need and start adding features.

## What's included

- **Spring Boot 4.1 on Java 25**, Maven wrapper, PostgreSQL 18, Redis 8
- **Redis caching** with Spring Cache: typed JSON values, TTL, eviction after commit, and a database fallback when Redis is down
- **Stateless JWT auth** using Spring Security's resource server: login with username or email,
  tokens as `Authorization: Bearer` or an HttpOnly `SameSite=Strict` cookie, remember-me, refresh, logout
- **Brute-force protection**: per-IP and per-account lockout with `429` + `Retry-After`
- **Users**: registration, profile and password change, admin CRUD with pagination, role-based access via `@PreAuthorize`
- **Standard HTTP semantics**: versioned URLs (`/api/v1`), correct status codes, `201` + `Location`,
  [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) `application/problem+json` errors for everything, including 401/403
- **Typed, validated configuration** (`app.*`) from environment variables or a `.env` file, so a missing secret fails at startup
- **Tests**: unit tests plus end-to-end tests against PostgreSQL with Testcontainers, with a JaCoCo coverage report
- **OpenAPI / Swagger UI**, Actuator health probes, Docker image, Compose stack, GitHub Actions CI
- **Doxygen docs** for every class and method, enforced in CI
- **Scripts** for every task, as `scripts/*.sh` and `scripts\*.bat`

## Quick start

Prerequisites: **JDK 25** (a JRE is not enough) and **Docker or Podman**.

```bash
scripts/setup.sh    # checks prerequisites, creates .env with generated secrets, compiles
scripts/dev.sh      # starts PostgreSQL + Redis and the app on http://localhost:8080
scripts/smoke.sh    # in another terminal: register, login, /me, refresh, logout against the running app
```

On Windows use the same names with `.bat`: `scripts\setup.bat`, `scripts\dev.bat`, `scripts\smoke.bat`.

Open http://localhost:8080/swagger-ui.html to try the API. `setup` prints the generated password of the
initial admin user (`admin`), which is created on first start.

## Scripts

| Script | What it does |
|--------|--------------|
| `setup` | Checks JDK 25 and a container runtime, creates `.env` from `.env.example` with a random `JWT_SECRET` and admin password, compiles. `--force` recreates `.env`. |
| `dev` | Starts PostgreSQL and Redis, then runs the app from source with `spring-boot:run`. |
| `db` | Local PostgreSQL and Redis: `up`, `down`, `reset` (deletes data), `logs`, `psql`, `redis-cli`, `status`. |
| `test` | Unit and integration tests plus coverage report (`target/site/jacoco/index.html`). Extra args go to Maven, e.g. `-Dtest=UserServiceTest`. |
| `build` | Builds `target/*.jar`; runs the tests unless `--skip-tests`. |
| `run` | Runs the built jar with the settings from `.env`. |
| `docker` | Whole stack in containers (PostgreSQL + Redis + app image): `up`, `down`, `logs`, `ps`. |
| `smoke` | End-to-end check against a running instance: `scripts/smoke.sh [base-url]`. |
| `docs` | Doxygen HTML in `target/doxygen/html`. `--strict` fails on any Doxygen warning, including undocumented code. |
| `rename-project` | Renames Maven coordinates, app name and Java package: `scripts/rename-project.sh com.acme order-service com.acme.orders`. |

On Linux, `test` uses rootless Podman automatically when Docker isn't available. It starts `podman.socket` and points Testcontainers at it.
Windows `.bat` scripts need Docker Desktop, or Podman with its Docker API enabled. `rename-project.bat` runs the shell script through Git Bash.

## Configuration

Everything is set through environment variables, or a `.env` file in the working directory, which
both the app and Compose read. [`.env.example`](.env.example) lists all of them:

| Variable | Default | Purpose |
|----------|---------|---------|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | `jdbc:postgresql://localhost:5432/app`, `app`, `app` | Database connection |
| `REDIS_HOST`, `REDIS_PORT` | `localhost`, `6379` | Redis connection (cache) |
| `CACHE_TTL` | `10m` | How long cached entries live |
| `JWT_SECRET` | *(required)* | HMAC-SHA256 signing secret, at least 32 characters |
| `JWT_ACCESS_TOKEN_TTL` / `JWT_REMEMBER_ME_TTL` | `15m` / `30d` | Token lifetimes |
| `COOKIE_SECURE` | `false` | Set `true` behind HTTPS |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Comma-separated browser origins |
| `LOGIN_MAX_ATTEMPTS` / `LOGIN_LOCK_DURATION` | `10` / `1h` | Brute-force lockout |
| `ADMIN_USERNAME` / `ADMIN_EMAIL` / `ADMIN_PASSWORD` | *(none; `setup` fills in `admin` / `admin@example.com` / a generated password)* | Initial admin, created only when all three are set and no admin exists |
| `FORWARD_HEADERS_STRATEGY` | `none` | Set `framework` behind a trusted proxy, so the real client IP is used |
| `SWAGGER_ENABLED` | `true` | Disable Swagger UI and API docs in production |
| `PORT` | `8080` | HTTP port |

The full list of properties is in [`application.yml`](src/main/resources/application.yml).

## API

| Method | Path | Access | Success |
|--------|------|--------|---------|
| `POST` | `/api/v1/auth/register` | public | `201` + `Location` |
| `POST` | `/api/v1/auth/login` | public | `200` token + cookie |
| `POST` | `/api/v1/auth/refresh` | authenticated | `200` token + cookie |
| `POST` | `/api/v1/auth/logout` | public | `204` (clears cookie) |
| `GET` / `PATCH` | `/api/v1/users/me` | authenticated | `200` |
| `PUT` | `/api/v1/users/me/password` | authenticated | `204` |
| `GET` | `/api/v1/users?page=0&size=20&sort=createdAt,desc` | admin | `200` page |
| `GET` | `/api/v1/users/{id}` | admin or that user | `200` |
| `PATCH` / `DELETE` | `/api/v1/users/{id}` | admin | `200` / `204` |
| `GET` | `/actuator/health`, `/v3/api-docs`, `/swagger-ui.html` | public | `200` (Swagger UI redirects to `/swagger-ui/index.html`) |

```bash
curl -i -X POST localhost:8080/api/v1/auth/register -H 'Content-Type: application/json' \
  -d '{"username":"alice","email":"alice@example.com","password":"Secret#123"}'

TOKEN=$(curl -s -X POST localhost:8080/api/v1/auth/login -H 'Content-Type: application/json' \
  -d '{"identifier":"alice","password":"Secret#123"}' | sed -E 's/.*"accessToken":"([^"]+)".*/\1/')

curl -s localhost:8080/api/v1/users/me -H "Authorization: Bearer $TOKEN"
```

Login returns `{"accessToken":"…","tokenType":"Bearer","expiresIn":900}`, shaped like an OAuth 2.0 token response.
Pages look like `{"content":[…],"page":{"size":20,"number":0,"totalElements":42,"totalPages":3}}`.

Every error is a problem detail:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Request validation failed.",
  "instance": "/api/v1/auth/register",
  "errors": [{ "field": "email", "message": "must be a well-formed email address" }]
}
```

## Project structure

```
src/main/java/com/hikmethankolay/template/
├── auth/        AuthController, AuthService, request/response DTOs
├── user/        User entity, repository, service, controller, initial-admin bootstrap, validation
├── security/    filter chains, JWT encoder/decoder, TokenService, login lockout
├── common/      ApiException, GlobalExceptionHandler (RFC 9457)
└── config/      AppProperties (typed app.* config), Redis cache, OpenAPI
src/main/resources/
├── application.yml   all configuration, overridable via env/.env
└── schema.sql        database schema, applied on startup
scripts/              *.sh and *.bat for every task
compose.yaml          PostgreSQL + Redis (+ app with --profile app)
Dockerfile            multi-stage, layered jar, non-root JRE image
```

Code is organised by feature. A new feature gets its own package next to `user/`, with its own
controller, service, repository and `dto/`.

## Starting a new project from this template

1. Copy the repository, or use it as a GitHub template.
2. `scripts/rename-project.sh <groupId> <artifactId> [basePackage]`, then `scripts/test.sh`.
3. Add your tables to `schema.sql` and your feature packages under the base package.
4. Throw domain errors with `ApiException.notFound(...)`, `.conflict(...)` and so on. They become problem details automatically.
5. Protect endpoints with `@PreAuthorize`. Everything outside the public paths in `SecurityConfig` requires a valid token by default.
6. Delete the user/auth features if you don't need them, along with their tests.

## Caching

User lookups by id are cached in Redis (`config/CacheConfig.java`). To cache something new:

1. Add a name to `CacheConfig.CacheNames` and register its value type in `redisCacheCustomizer`. Values are stored
   as JSON of that exact type, never with Java serialization.
2. Put `@Cacheable(cacheNames = ..., key = "#id")` on the read method, and `@CacheEvict` on every method that changes the data.
3. Cache DTOs, not JPA entities.

Evictions inside a transaction are applied only after the commit. If Redis is unreachable, the error is logged
and requests go straight to the database. Redis is left out of `/actuator/health` for the same reason.
Inspect the cache with `scripts/db.sh redis-cli`, e.g. `KEYS spring-boot-template:*`.

## Testing and documentation

```bash
scripts/test.sh            # unit + integration tests, JaCoCo report in target/site/jacoco
scripts/docs.sh --strict   # Doxygen HTML in target/doxygen/html; fails on any warning, including undocumented code
```

Integration tests start throwaway `postgres:18-alpine` and `redis:8-alpine` containers through Testcontainers. Without a
container runtime they are skipped, not failed. CI (`.github/workflows/ci.yml`) runs `./mvnw -B verify` and
`scripts/docs.sh --strict` on pushes to `main` and on pull requests.

## Before going to production

These are deliberate simplifications. Revisit them for your project:

- **Schema**: `schema.sql` runs idempotently on every start. Switch to Flyway or Liquibase once the schema needs to evolve.
- **Lockout state** is in memory and per instance. Use Redis or similar when running several instances.
- **Tokens** are signed with a shared HS256 secret and can't be revoked before they expire. Keep TTLs short, or add a denylist,
  or use an external identity provider (Keycloak, Auth0, ...) and keep only the resource-server part.
- **Production settings**: `COOKIE_SECURE=true`, real `CORS_ALLOWED_ORIGINS`, `FORWARD_HEADERS_STRATEGY=framework` behind a proxy,
  and `SWAGGER_ENABLED=false` if the API docs shouldn't be public.

## License

[MIT](LICENSE) © 2025-2026 Hikmethan Kolay

## Author

- **Hikmethan Kolay** - [GitHub](https://github.com/hikmethankolay)
- **Email**: hikmethan.kolay@gmail.com
