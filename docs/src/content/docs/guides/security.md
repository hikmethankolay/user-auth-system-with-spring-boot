---
title: Security
description: Authentication, token revocation, account flows, lockout and rate limiting.
---

## Authentication

Authentication is stateless, using JWTs and Spring Security's OAuth2 resource server.

- `POST /api/v1/auth/login` accepts a username or an email address. The token comes back in the body
  (`{"accessToken", "tokenType", "expiresIn"}`) and as an HttpOnly `SameSite=Strict` cookie.
- Tokens are sent as `Authorization: Bearer …` or as the cookie. Tokens are HS256-signed with `JWT_SECRET`, carry
  `sub`, `roles`, `username`, a unique `jti`, and expire after `JWT_ACCESS_TOKEN_TTL`, or `JWT_REMEMBER_ME_TTL` with
  `"rememberMe": true`.
- Public endpoints have their own filter chain without token processing, so a stale cookie can never block login.
- Everything else requires a valid token. Role checks use `@PreAuthorize("hasRole('ADMIN')")`.
- CSRF protection is off, because the API is stateless and the cookie is `SameSite=Strict`.

## Revocation

Logout revokes the presented token, and refresh revokes the old token. Revoked token ids are kept in Redis until the
token would have expired, and the JWT decoder rejects them on every request, on every instance.

## Account flows

| Endpoint | Purpose |
|----------|---------|
| `POST /api/v1/auth/verify-email` | Confirm an address with the token from the email |
| `POST /api/v1/auth/verify-email/resend` | Send a new verification email (`202`) |
| `POST /api/v1/auth/password-reset` | Send a reset email (`202`) |
| `POST /api/v1/auth/password-reset/confirm` | Set a new password with the reset token |

- Tokens are 256-bit random values. Only their SHA-256 hash is stored.
- Tokens are single-use and expire after 24 hours (verification) or 1 hour (reset). Requesting a new one invalidates
  the previous one.
- The `202` endpoints answer the same way for unknown addresses, so they cannot be used to find accounts.
- Changing an email marks it unverified and sends a new verification email.
- With `REQUIRE_VERIFIED_EMAIL=true` (the prod default), unverified users get `403` at login.

## Brute-force protection

Failed logins are counted per client IP and per account in Redis. After `LOGIN_MAX_ATTEMPTS` failures, the login is
rejected with `429` and `Retry-After`, even with the right password, until `LOGIN_LOCK_DURATION` has passed without
new failures. A successful login only resets the account counter, not the IP counter.

## Rate limiting

Every client IP gets `RATE_LIMIT_PER_MINUTE` requests per minute across all instances. Responses carry
`RateLimit-Limit`, `RateLimit-Remaining` and `RateLimit-Reset`. Over the limit, the API answers `429` with
`Retry-After`. Actuator is exempt.

:::caution[Behind a proxy]
Set `FORWARD_HEADERS_STRATEGY=framework` and make sure the proxy overwrites `X-Forwarded-For`. Otherwise every client
appears to have the proxy's IP, and one client can lock out everyone.
:::

## Redis outages

The lockout, rate limit and denylist fail open: if Redis is unreachable, requests are allowed and a warning is logged.
That keeps the API available, at the cost of briefly disabling these protections. See [Production](../production/).
