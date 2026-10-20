---
title: Configuration
description: Environment variables and the .env file.
---

Everything is set through environment variables, or a `.env` file in the working directory, which both the app and
Docker Compose read. Tests never read `.env`: the build points `DOTENV_PATH` at a file that does not exist, and you
can use the same variable to load another file, e.g. `DOTENV_PATH=staging.env java -jar target/*.jar`. [`.env.example`](https://github.com/hikmethankolay/spring-boot-backend-template/blob/main/.env.example)
lists the common variables, and `scripts/setup.sh` creates `.env` from it with generated secrets.

The `app.*` settings are bound to a validated record (`AppProperties`), so a missing or invalid value such as a short
`JWT_SECRET` stops the application at startup with a clear message.

| Variable | Default | Purpose |
|----------|---------|---------|
| `SPRING_PROFILES_ACTIVE` | *(none)* | `prod` for production defaults, see [Production](../production/) |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | `jdbc:postgresql://localhost:5432/app`, `app`, `app` | Database connection |
| `DB_NAME` / `DB_PORT` | `app` / `5432` | Database name and host port of the local `db` container (compose only; the app uses `DB_URL`) |
| `DB_POOL_SIZE` / `DB_CONNECTION_TIMEOUT_MS` | `10` / `5000` | Connections per instance; how long a request waits for one |
| `DB_STATEMENT_TIMEOUT` / `DB_IDLE_IN_TRANSACTION_TIMEOUT` | `30s` / `60s` | PostgreSQL cancels longer queries and idle transactions (`0` disables) |
| `REDIS_HOST`, `REDIS_PORT` | `localhost`, `6379` | Redis: cache, lockout, rate limit, token revocation |
| `REDIS_PASSWORD` / `REDIS_SSL_ENABLED` | *(none)* / `false` | Managed Redis with authentication and TLS; add `SPRING_DATA_REDIS_USERNAME` for an ACL user |
| `CACHE_TTL` | `10m` | How long cached entries live |
| `JWT_SECRET` | *(required)* | HMAC-SHA256 signing secret, at least 32 characters |
| `JWT_ACCESS_TOKEN_TTL` | `15m` | Access token (JWT) lifetime; keep it short |
| `JWT_REFRESH_TOKEN_TTL` / `JWT_REMEMBER_ME_TTL` | `1d` / `30d` | Session lifetime without activity, normal / remember-me |
| `COOKIE_SECURE` | `false` (`true` in prod) | Send the auth cookies over HTTPS only |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Comma-separated browser origins |
| `LOGIN_MAX_ATTEMPTS` / `LOGIN_LOCK_DURATION` | `10` / `1h` | Brute-force lockout |
| `RATE_LIMIT_ENABLED` / `RATE_LIMIT_PER_MINUTE` | `true` / `120` | Per-IP API rate limit |
| `REQUIRE_VERIFIED_EMAIL` | `false` (`true` in prod) | Block login until the email address is verified |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_FROM` | `localhost`, `1025`, `no-reply@example.com` | Outgoing mail (Mailpit by default). Set `SPRING_MAIL_USERNAME` / `SPRING_MAIL_PASSWORD` for a real SMTP server |
| `MAIL_LINK_BASE_URL` | `http://localhost:3000` | Frontend that handles `/verify-email?token=…` and `/reset-password?token=…` |
| `MAIL_MAX_ATTEMPTS` / `MAIL_RETRY_DELAY` / `MAIL_OUTBOX_POLL_INTERVAL` | `10` / `30s` / `10s` | Email outbox retries (the delay doubles after each failure, up to 1 hour) |
| `ADMIN_USERNAME` / `ADMIN_EMAIL` / `ADMIN_PASSWORD` | *(none; `setup` fills them in)* | The owner account (role `OWNER`, see [Security](../security/#roles)), created only when all three are set and no owner exists |
| `TRACING_SAMPLING_PROBABILITY` | `1.0` (`0.1` in prod) | Share of requests traced |
| `MANAGEMENT_OPENTELEMETRY_TRACING_EXPORT_OTLP_ENDPOINT` | *(none)* | OTLP collector, e.g. `http://otel-collector:4318/v1/traces` |
| `OTLP_METRICS_EXPORT_ENABLED` / `MANAGEMENT_OTLP_METRICS_EXPORT_URL` | `false` / `http://localhost:4318/v1/metrics` | Also push metrics to an OTLP collector (Prometheus scraping works either way) |
| `MANAGEMENT_PORT` | *(same port; `8081` in prod)* | Actuator port in the prod profile |
| `FORWARD_HEADERS_STRATEGY` | `none` | `framework` behind a trusted proxy, so the real client IP is used |
| `SWAGGER_ENABLED` | `true` (`false` in prod) | Swagger UI and API docs |
| `PORT` | `8080` | HTTP port |

All properties are in
[`application.yml`](https://github.com/hikmethankolay/spring-boot-backend-template/blob/main/src/main/resources/application.yml),
and the production overrides are in
[`application-prod.yml`](https://github.com/hikmethankolay/spring-boot-backend-template/blob/main/src/main/resources/application-prod.yml).

## Database migrations

The schema is managed by Flyway (`src/main/resources/db/migration`), and Hibernate only validates that the entities
match it. Databases created before Flyway was introduced are baselined at version 1 instead of failing.

Migrations run with the same `statement_timeout` as the application. For a migration that needs longer (a large
backfill or index), start that release once with `DB_STATEMENT_TIMEOUT=0`, or use `SET LOCAL statement_timeout` inside
the migration.
