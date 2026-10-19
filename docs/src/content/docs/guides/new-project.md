---
title: Using the template
description: Turn the template into your own project.
---

1. Copy the repository, or use it as a GitHub template.
2. Rename it:

   ```bash
   scripts/rename-project.sh com.acme order-service com.acme.orders
   scripts/test.sh
   ```

   The script changes the Maven `groupId` and `artifactId`, the application name and the base Java package, and moves
   the source files. If you leave out the package, it is derived from the other two (`com.acme.orderservice`).
   It refuses to run twice.
3. Add your tables as new Flyway migrations (`V3__create_orders.sql`, ...) in `src/main/resources/db/migration`.
   Never edit a migration that has already been applied.
4. Add each feature as its own package next to `user/`, with its own controller, service, repository and `dto/`.
5. Throw domain errors with `ApiException.notFound(...)`, `.conflict(...)`, `.badRequest(...)`. They become
   RFC 9457 problem details automatically.
6. Protect endpoints with `@PreAuthorize`. Everything outside the public paths in `SecurityConfig` needs a valid token.
7. Delete the features you don't need (for example `account/`), along with their tests and migrations.

## Rules the build enforces

`ArchitectureTest` (ArchUnit) fails the build if:

- a controller uses a repository or a JPA entity directly
- anything depends on a controller
- a class uses field injection, `System.out` or `java.util.logging`

`scripts/docs.sh --strict` fails when a public class, method or field has no documentation comment.

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
└── db/migration/          Flyway migrations
docs/                Astro Starlight documentation site
load-tests/          k6 scripts
scripts/             *.sh and *.bat for every task
```
