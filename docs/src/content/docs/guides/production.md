---
title: Production
description: The prod profile, deployment and the deliberate trade-offs.
---

## The prod profile

Start the app with `SPRING_PROFILES_ACTIVE=prod` to switch to:

- ECS JSON logs
- `Secure` cookies
- required email verification
- Swagger UI and API docs disabled
- 10% trace sampling
- Actuator on port `8081`, which must stay internal

Each setting can still be overridden with its environment variable.

## Deploying

The Docker image (multi-stage, layered jar, non-root JRE on Alpine) exposes `8080` for the API and `8081` for Actuator.
Pushing a `v*` tag runs the release workflow:

1. It runs the tests.
2. It publishes the image to `ghcr.io/<owner>/<repo>`.
3. It creates a GitHub release, and the documentation site is rebuilt.

```bash
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=... -e DB_USERNAME=... -e DB_PASSWORD=... \
  -e REDIS_HOST=... -e JWT_SECRET=... -e MAIL_HOST=... \
  ghcr.io/hikmethankolay/user-auth-system-with-spring-boot:latest
```

## Checklist

- [ ] Set real values for `CORS_ALLOWED_ORIGINS` and `MAIL_LINK_BASE_URL`.
- [ ] Use an SMTP server (`MAIL_HOST`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD`).
- [ ] Behind a proxy, set `FORWARD_HEADERS_STRATEGY=framework` and let the proxy overwrite `X-Forwarded-For`.
- [ ] Keep port `8081` internal and scrape metrics there.
- [ ] Generate a long random `JWT_SECRET` and store it in your secret manager.

## Trade-offs to revisit

- **Fail-open on Redis outages.** The cache, login lockout, rate limit and token denylist keep the API available
  when Redis is down, at the cost of briefly disabling those protections. Run Redis with high availability, or change
  the handlers to fail closed.
- **Redis memory.** Redis holds security state, so it runs with `noeviction`. Size it for your traffic; every key has
  a TTL.
- **Revocation scope.** Logout and refresh revoke tokens, but changing or resetting a password does not revoke other
  sessions' tokens. Keep `JWT_ACCESS_TOKEN_TTL` short, or add per-user revocation. For SSO or many services, consider
  an external identity provider and keep only the resource-server part.
- **Dependency overrides.** `pom.xml` pins a few patched dependency versions. Remove each override once a Spring Boot
  upgrade manages an equal or newer version.
