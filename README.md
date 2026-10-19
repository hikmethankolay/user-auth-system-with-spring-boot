# Spring Boot Backend Template

A production-ready starting point for Spring Boot REST backends: JWT authentication with revocation,
user and account management, caching, rate limiting, observability, database migrations, tests against
real infrastructure, and one-command scripts for Linux, macOS and Windows.

Clone it, run `scripts/rename-project.sh`, delete what you don't need and start adding features.

## What's included

- **Spring Boot 4.1 on Java 25**, Maven wrapper, PostgreSQL 18 with **Flyway** migrations, Redis 8
- **Stateless JWT auth** with Spring Security's resource server: login by username or email, Bearer header or
  HttpOnly `SameSite=Strict` cookie, remember-me, refresh, and **real logout** (revoked tokens are rejected)
- **Account flows**: email verification and password reset with single-use, hashed, expiring tokens
- **Abuse protection** shared across instances through Redis: per-IP and per-account login lockout, and a per-IP
  API rate limit with `RateLimit-*` headers, `429` and `Retry-After`
- **Users**: profile and password change, admin CRUD with pagination, role checks with `@PreAuthorize`
- **Redis caching** with typed JSON values, TTL, eviction after commit and a database fallback
- **Standard HTTP**: versioned URLs (`/api/v1`), correct status codes, `201` + `Location`, and
  [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) `application/problem+json` for every error, including 401/403/429
- **Observability**: Prometheus metrics, OpenTelemetry tracing, trace ids in every log line and in an `X-Trace-Id` header,
  ECS JSON logs in production
- **Profiles**: safe defaults with `SPRING_PROFILES_ACTIVE=prod` (secure cookies, verified emails, no API docs,
  Actuator on an internal port)
- **Tests**: unit, integration (Testcontainers: PostgreSQL + Redis), architecture rules (ArchUnit) and a k6 load test
- **Supply chain**: Dependabot, Trivy image scan in CI, release workflow publishing to GHCR
- **Doxygen docs** for every class and method, enforced in CI; OpenAPI / Swagger UI
- **Scripts** for every task, as `scripts/*.sh` and `scripts\*.bat`

## Quick start

Prerequisites: **JDK 25** (a JRE is not enough) and **Docker or Podman**.

```bash
scripts/setup.sh    # checks prerequisites, creates .env with generated secrets, compiles
scripts/dev.sh      # starts PostgreSQL, Redis and Mailpit, then the app on http://localhost:8080
scripts/smoke.sh    # in another terminal: register, login, /me, refresh, logout against the running app
```

On Windows use the same names with `.bat`: `scripts\setup.bat`, `scripts\dev.bat`, `scripts\smoke.bat`.

- API docs: http://localhost:8080/swagger-ui.html
- Emails sent in development (verification, password reset): http://localhost:8025 (Mailpit)
- `setup` prints the generated password of the initial admin user (`admin`), created on first start.

## Scripts

| Script | What it does |
|--------|--------------|
| `setup` | Checks JDK 25 and a container runtime, creates `.env` from `.env.example` with a random `JWT_SECRET` and admin password, compiles. `--force` recreates `.env`. |
| `dev` | Starts PostgreSQL, Redis and Mailpit, then runs the app from source with `spring-boot:run`. |
| `db` | Local services: `up`, `down`, `reset` (deletes data), `logs`, `psql`, `redis-cli`, `status`. |
| `test` | Unit, integration and architecture tests plus coverage report (`target/site/jacoco/index.html`). Extra args go to Maven, e.g. `-Dtest=UserServiceTest`. |
| `build` | Builds `target/*.jar`; runs the tests unless `--skip-tests`. |
| `run` | Runs the built jar with the settings from `.env`. |
| `docker` | Whole stack in containers (PostgreSQL, Redis, Mailpit, app image): `up`, `down`, `logs`, `ps`. |
| `smoke` | End-to-end check against a running instance: `scripts/smoke.sh [base-url]`. |
| `load-test` | k6 load test against a running instance (start it with `RATE_LIMIT_ENABLED=false`): `VUS=50 DURATION=1m scripts/load-test.sh [base-url]`. `THINK_TIME=0` measures maximum throughput (about 7,500 req/s on a 20-core dev machine). |
| `docs` | Doxygen HTML in `target/doxygen/html`. `--strict` fails on any Doxygen warning, including undocumented code. |
| `rename-project` | Renames Maven coordinates, app name and Java package: `scripts/rename-project.sh com.acme order-service com.acme.orders`. |

On Linux, `test` uses rootless Podman automatically when Docker isn't available (it starts `podman.socket`).
Windows `.bat` scripts need Docker Desktop, or Podman with its Docker API enabled. `rename-project.bat` runs the
shell script through Git Bash.

## Configuration

Everything is set through environment variables, or a `.env` file in the working directory, which both the app and
Compose read (tests never read it). [`.env.example`](.env.example) lists the common ones:

| Variable | Default | Purpose |
|----------|---------|---------|
| `SPRING_PROFILES_ACTIVE` | *(none)* | `prod` for production defaults (see below) |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | `jdbc:postgresql://localhost:5432/app`, `app`, `app` | Database connection |
| `REDIS_HOST`, `REDIS_PORT` | `localhost`, `6379` | Redis (cache, lockout, rate limit, token denylist) |
| `CACHE_TTL` | `10m` | How long cached entries live |
| `JWT_SECRET` | *(required)* | HMAC-SHA256 signing secret, at least 32 characters |
| `JWT_ACCESS_TOKEN_TTL` / `JWT_REMEMBER_ME_TTL` | `15m` / `30d` | Token lifetimes |
| `COOKIE_SECURE` | `false` (`true` in prod) | Send the auth cookie over HTTPS only |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Comma-separated browser origins |
| `LOGIN_MAX_ATTEMPTS` / `LOGIN_LOCK_DURATION` | `10` / `1h` | Brute-force lockout |
| `RATE_LIMIT_ENABLED` / `RATE_LIMIT_PER_MINUTE` | `true` / `120` | Per-IP API rate limit |
| `REQUIRE_VERIFIED_EMAIL` | `false` (`true` in prod) | Block login until the email address is verified |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_FROM` | `localhost`, `1025` (Mailpit), `no-reply@example.com` | Outgoing mail; set `SPRING_MAIL_USERNAME` / `SPRING_MAIL_PASSWORD` for a real SMTP server |
| `MAIL_LINK_BASE_URL` | `http://localhost:3000` | Frontend that handles `/verify-email?token=…` and `/reset-password?token=…` |
| `ADMIN_USERNAME` / `ADMIN_EMAIL` / `ADMIN_PASSWORD` | *(none; `setup` fills them in)* | Initial admin, created only when all three are set and no admin exists |
| `TRACING_SAMPLING_PROBABILITY` | `1.0` (`0.1` in prod) | Share of requests traced |
| `MANAGEMENT_OPENTELEMETRY_TRACING_EXPORT_OTLP_ENDPOINT` | *(none)* | OTLP collector, e.g. `http://otel-collector:4318/v1/traces`; traces are only exported when set |
| `MANAGEMENT_PORT` | *(same as `PORT`; `8081` in prod)* | Actuator port in the prod profile |
| `FORWARD_HEADERS_STRATEGY` | `none` | Set `framework` behind a trusted proxy, so the real client IP is used |
| `SWAGGER_ENABLED` | `true` (`false` in prod) | Swagger UI and API docs |
| `PORT` | `8080` | HTTP port |

All properties are in [`application.yml`](src/main/resources/application.yml); production overrides are in
[`application-prod.yml`](src/main/resources/application-prod.yml).

### Production profile

`SPRING_PROFILES_ACTIVE=prod` switches to ECS JSON logs, Secure cookies, required email verification, disabled API docs,
10% trace sampling, and Actuator on port `8081`, which must stay internal. Each setting can still be overridden with
its environment variable.

## API

| Method | Path | Access | Success |
|--------|------|--------|---------|
| `POST` | `/api/v1/auth/register` | public | `201` + `Location`, sends a verification email |
| `POST` | `/api/v1/auth/login` | public | `200` token + cookie |
| `POST` | `/api/v1/auth/refresh` | authenticated | `200` new token + cookie; the old token is revoked |
| `POST` | `/api/v1/auth/logout` | public | `204`; revokes the presented token and clears the cookie |
| `POST` | `/api/v1/auth/verify-email` | public | `204` |
| `POST` | `/api/v1/auth/verify-email/resend` | public | `202` |
| `POST` | `/api/v1/auth/password-reset` | public | `202` |
| `POST` | `/api/v1/auth/password-reset/confirm` | public | `204` |
| `GET` / `PATCH` | `/api/v1/users/me` | authenticated | `200`; changing the email requires verifying it again |
| `PUT` | `/api/v1/users/me/password` | authenticated | `204` |
| `GET` | `/api/v1/users?page=0&size=20&sort=createdAt,desc` | admin | `200` page |
| `GET` | `/api/v1/users/{id}` | admin or that user | `200` |
| `PATCH` / `DELETE` | `/api/v1/users/{id}` | admin | `200` / `204` |
| `GET` | `/actuator/health`, `/actuator/prometheus` | public (internal port in prod) | `200` |
| `GET` | `/v3/api-docs`, `/swagger-ui.html` | public (off in prod) | `200` |

The two `202` endpoints answer the same way whether or not the address has an account.

```bash
curl -i -X POST localhost:8080/api/v1/auth/register -H 'Content-Type: application/json' \
  -d '{"username":"alice","email":"alice@example.com","password":"Secret#123"}'

TOKEN=$(curl -s -X POST localhost:8080/api/v1/auth/login -H 'Content-Type: application/json' \
  -d '{"identifier":"alice","password":"Secret#123"}' | sed -E 's/.*"accessToken":"([^"]+)".*/\1/')

curl -s localhost:8080/api/v1/users/me -H "Authorization: Bearer $TOKEN"
```

Login returns `{"accessToken":"…","tokenType":"Bearer","expiresIn":900}`, shaped like an OAuth 2.0 token response.
Pages look like `{"content":[…],"page":{"size":20,"number":0,"totalElements":42,"totalPages":3}}`.
Every response carries `X-Trace-Id` and the `RateLimit-Limit` / `RateLimit-Remaining` / `RateLimit-Reset` headers.

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
├── auth/        AuthController, AuthService (register, login, refresh, logout)
├── account/     email verification and password reset: tokens, mailer, endpoints
├── user/        User entity, repository, service, controller, initial-admin bootstrap, validation
├── security/    filter chains, JWT, token revocation, login lockout, rate limiting
├── common/      ApiException, RFC 9457 error handler, X-Trace-Id filter
└── config/      AppProperties (typed app.* config), Redis cache, OpenAPI
src/main/resources/
├── application.yml        all configuration, overridable via env/.env
├── application-prod.yml   production overrides
└── db/migration/          Flyway migrations, applied on startup
load-tests/           k6 scripts
scripts/              *.sh and *.bat for every task
compose.yaml          PostgreSQL, Redis, Mailpit (+ app with --profile app)
Dockerfile            multi-stage, layered jar, non-root JRE image
```

Code is organised by feature. A new feature gets its own package next to `user/`, with its own controller, service,
repository and `dto/`. `ArchitectureTest` enforces the basics: controllers use services, never repositories or entities.

## Starting a new project from this template

1. Copy the repository, or use it as a GitHub template.
2. `scripts/rename-project.sh <groupId> <artifactId> [basePackage]`, then `scripts/test.sh`.
3. Add your tables as new Flyway migrations (`V3__create_orders.sql`, ...) and your feature packages under the base package.
   Never edit a migration that has already been applied.
4. Throw domain errors with `ApiException.notFound(...)`, `.conflict(...)` and so on. They become problem details automatically.
5. Protect endpoints with `@PreAuthorize`. Everything outside the public paths in `SecurityConfig` requires a valid token.
6. Delete the features you don't need (e.g. `account/`), along with their tests and migrations.

## Caching

User lookups by id are cached in Redis (`config/CacheConfig.java`). To cache something new:

1. Add a name to `CacheConfig.CacheNames` and register its value type in `redisCacheCustomizer`. Values are stored
   as JSON of that exact type, never with Java serialization.
2. Put `@Cacheable(cacheNames = ..., key = "#id")` on the read method, and `@CacheEvict` on every method that changes the data.
3. Cache DTOs, not JPA entities.

Evictions inside a transaction are applied only after the commit. Inspect Redis with `scripts/db.sh redis-cli`,
e.g. `KEYS spring-boot-template:*`.

## Observability

- **Metrics**: `/actuator/prometheus`, tagged with `application`. Point Prometheus at it (on port 8081 in prod).
- **Tracing**: requests are traced with OpenTelemetry. Trace and span ids appear in every log line, and the trace id is
  returned as `X-Trace-Id`, so a client's error report leads straight to the matching logs. Set the OTLP endpoint
  variable to export traces to a collector (Jaeger, Tempo, ...).
- **Logs**: readable text locally, one ECS JSON object per line in the prod profile.

## Testing and documentation

```bash
scripts/test.sh            # unit, integration and ArchUnit tests; JaCoCo report in target/site/jacoco
scripts/docs.sh --strict   # Doxygen HTML in target/doxygen/html; fails on any warning, including undocumented code
scripts/load-test.sh       # k6: fails if p95 > 300 ms or more than 1% of requests fail
```

Integration tests share one throwaway `postgres:18-alpine` and one `redis:8-alpine` container (Testcontainers);
outgoing mail is captured in memory. Without a container runtime they are skipped, not failed.

CI (`.github/workflows/ci.yml`) runs the tests, builds the image and scans it with Trivy (failing on fixable critical or
high vulnerabilities), and checks the documentation. Pushing a `v*` tag runs `.github/workflows/release.yml`, which
publishes the image to `ghcr.io/<owner>/<repo>` and creates a GitHub release. Dependabot opens weekly update PRs.

## Before going to production

These are deliberate trade-offs. Revisit them for your project:

- **Fail-open on Redis outages**: the cache, login lockout, rate limit and token denylist all keep the API available
  when Redis is down, at the cost of briefly disabling those protections. Run Redis with high availability, or change
  the handlers to fail closed.
- **Redis memory**: Redis holds security state, so it runs with `noeviction`; size it for your traffic (every key has a TTL).
- **Token revocation** covers logout and refresh. Changing or resetting a password does not revoke other sessions' tokens:
  keep `JWT_ACCESS_TOKEN_TTL` short, or add per-user revocation. For SSO or many services, consider an external identity
  provider (Keycloak, Auth0, ...) and keep only the resource-server part.
- **Proxies**: behind a load balancer set `FORWARD_HEADERS_STRATEGY=framework` and make sure the proxy overwrites
  `X-Forwarded-For`; otherwise rate limits and lockouts see the proxy's IP.
- **Security patches**: `pom.xml` overrides a few dependency versions with security fixes; remove each override once
  a Spring Boot upgrade manages an equal or newer version.

## License

[MIT](LICENSE) © 2025-2026 Hikmethan Kolay

## Author

- **Hikmethan Kolay** - [GitHub](https://github.com/hikmethankolay)
- **Email**: hikmethan.kolay@gmail.com
