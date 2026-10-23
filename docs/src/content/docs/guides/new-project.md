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
   The sample `note/` package shows all of it; see [Your first feature](../first-feature/), which also says how to
   delete it.
5. After changing an endpoint, regenerate the committed OpenAPI document with
   `scripts/test.sh -Dit.test=OpenApiSpecExportIntegrationTest -Dopenapi.update=true`, give new operations an `operationId`, and
   declare responses the shared customizers cannot know about (for example a 409 on a uniqueness conflict).
6. For every list endpoint, allow sorting only by fields the response already shows: check the `Pageable` with a
   `SortAllowList` in the service (see `UserService.SORTABLE_PROPERTIES`) and name the properties in the `sort`
   parameter description.
7. Throw domain errors with `ApiException.notFound(...)`, `.conflict(...)`, `.badRequest(...)`. They become
   RFC 9457 problem details automatically.
8. Protect endpoints with `@PreAuthorize`. Everything outside the public paths in `SecurityConfig` needs a valid token.
9. If the feature stores data about a user, reference `users(id)` with `ON DELETE CASCADE` and implement
   `PersonalDataProvider`, so account deletion and the data export (`GET /api/v1/users/me/export`) stay complete.
10. Record sensitive actions with `AuditLog.record(AuditEvent.…, userId, ...)` and send emails with `EmailOutbox.enqueue`.
11. Delete the features you don't need (for example `account/`), along with their tests and migrations.
12. Replace the repository-specific values: the owner in `.github/CODEOWNERS`, the links in `SECURITY.md` and the
    issue templates, and the image name in `deploy/kubernetes/kustomization.yaml` and `deployment.yaml`. Enable
    *Private vulnerability reporting* in the repository's security settings.

## Getting fixes from the template later

A project made from the template does not share its git history, so updates are not merged automatically. Watch the
repository's releases (*Watch → Custom → Releases*); every release lists its changes in the
[changelog](https://github.com/hikmethankolay/spring-boot-backend-template/blob/main/CHANGELOG.md), and security fixes
are under a **Security** heading. To bring a change over, fetch the template as a second remote and cherry-pick the
commits named in the release notes:

```bash
git remote add template https://github.com/hikmethankolay/spring-boot-backend-template.git
git fetch template --tags
git log --oneline v2.9.2..template/main        # what changed since the version you started from
git cherry-pick -x <commit>                      # -x records where the change came from
```

`rename-project` moved the Java files and rewrote their package lines, so git usually follows the move but reports
conflicts on the `package` and `import` lines: keep your package names and the template's change. For a commit that
touches many files, `git diff <old-tag> <new-tag> -- src/main/resources` and similar per-directory diffs are often
easier to apply by hand. Note the template version you started from (`git describe --tags` in the template, or the
first entry of its changelog) in your own README.

## Rules the build enforces

`ArchitectureTest` (ArchUnit) fails the build if:

- a controller uses a repository or a JPA entity directly
- anything depends on a controller
- a class uses field injection, `System.out` or `java.util.logging`

`scripts/docs.sh` fails when a public, protected or package-private class, method or field has no Javadoc comment.

## Project structure

```
src/main/java/com/hikmethankolay/template/
├── auth/        login, refresh-token rotation, logout, session revocation
├── account/     email verification and password reset: tokens, mailer, endpoints
├── user/        User entity, service, controller, account deletion, data export, admin bootstrap
├── note/        sample feature: notes owned by a user (copy it, then delete it)
├── security/    filter chains, headers, JWT, token revocation, login lockout, rate limiting
├── audit/       AuditLog and AuditEvent: the security audit trail
├── mail/        EmailOutbox: transactional outbox with retries
├── common/      ApiException, RFC 9457 error handler, X-Trace-Id filter, SecureTokens
└── config/      AppProperties (typed app.* config), Redis cache, OpenAPI
src/main/resources/
├── application.yml        all configuration, overridable via env/.env
├── application-prod.yml   production overrides
└── db/migration/          Flyway migrations
deploy/kubernetes/   Kustomize manifests
ops/                 Prometheus alert rules, Grafana dashboard
docs/                Astro Starlight documentation site
load-tests/          k6 scripts
scripts/             *.sh and *.bat for every task
```
