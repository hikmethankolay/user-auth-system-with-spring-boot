---
title: Security
description: Tokens and sessions, revocation, account flows, lockout, rate limiting, headers and the audit log.
---

## Tokens and sessions

Authentication is stateless, using JWTs and Spring Security's OAuth2 resource server, plus a refresh token per session.

- `POST /api/v1/auth/login` accepts a username or an email address and returns two tokens, in the body
  (`{"accessToken", "tokenType", "expiresIn", "refreshToken"}`) and as HttpOnly `SameSite=Strict` cookies.
- The **access token** is an HS256 JWT signed with `JWT_SECRET`. It carries `sub`, `roles`, `username` and a unique
  `jti`, and expires after `JWT_ACCESS_TOKEN_TTL` (15 minutes). Send it as `Authorization: Bearer …` or as the
  `auth_token` cookie.
- The **refresh token** is an opaque 256-bit random value; only its SHA-256 hash is stored (table `refresh_tokens`).
  Exchange it at `POST /api/v1/auth/refresh` (body `{"refreshToken": "…"}`, or the `refresh_token` cookie, which the
  browser only sends to `/api/v1/auth`) for a new access token **and** a new refresh token.
- A session ends after `JWT_REFRESH_TOKEN_TTL` (1 day) without a refresh, or `JWT_REMEMBER_ME_TTL` (30 days) when
  logging in with `"rememberMe": true`. Remember-me sessions get persistent cookies; others get session cookies.
- Public endpoints, including `/refresh`, have their own filter chain without token processing, so an expired or stale
  access token can never block login or refresh.
- Everything else requires a valid access token. Role checks use `@PreAuthorize("hasRole('ADMIN')")`.
- CORS exposes every response header the API documents (`X-Trace-Id`, `RateLimit-*`, `Retry-After`, `Location`,
  `Content-Disposition`) to the origins in `CORS_ALLOWED_ORIGINS`.
- CSRF protection is off, because the API is stateless and the cookies are `SameSite=Strict`.

A client keeps calling the API with the access token, and when it gets `401`, calls `/refresh` once and retries.

### Rotation and reuse detection

Each refresh token can be exchanged **once**. Every token issued from one login belongs to the same family (the
session). If an already exchanged token is presented again, someone has copied it: the whole session is revoked,
both parties must log in again, and an `auth.refresh_token_reused` audit event is written.

Two requests refreshing the same token at the same time (for example two browser tabs) look the same as reuse. Let
one tab refresh and share the result, for example with a `BroadcastChannel` or a lock.

## Revocation

| Event | Effect |
|-------|--------|
| Logout | The presented access token's `jti` is denylisted and the refresh token's session is deleted |
| Password change, password reset | Every session of the user ends: refresh tokens are deleted and all access tokens issued so far are rejected |
| Admin changes a user's password or role | Same, so a demoted administrator loses access immediately |
| User deleted (by an admin or themselves) | Same; the refresh tokens are deleted with the user |

Access-token revocation lives in Redis: denylisted token ids and per-user cut-off times are kept only until the
affected tokens would have expired, and the JWT decoder checks both in one round trip on every request, on every
instance. After a password change the client must log in again.

## Account flows

| Endpoint | Purpose |
|----------|---------|
| `POST /api/v1/auth/verify-email` | Confirm an address with the token from the email |
| `POST /api/v1/auth/verify-email/resend` | Send a new verification email (`202`) |
| `POST /api/v1/auth/password-reset` | Send a reset email (`202`) |
| `POST /api/v1/auth/password-reset/confirm` | Set a new password with the reset token |
| `DELETE /api/v1/users/me` | Delete the own account; body `{"password": "…"}` |
| `GET /api/v1/users/me/export` | Download everything stored about the user as JSON |

- Tokens are 256-bit random values. Only their SHA-256 hash is stored.
- Tokens are single-use and expire after 24 hours (verification) or 1 hour (reset). Requesting a new one invalidates
  the previous one.
- The `202` endpoints answer the same way for unknown addresses, so they cannot be used to find accounts.
- Changing an email marks it unverified and sends a new verification email.
- With `REQUIRE_VERIFIED_EMAIL=true` (the prod default), unverified users get `403` at login.
- Emails go through an outbox with retries, see [Observability](../observability/#email-outbox).

## Personal data (GDPR / KVKK)

`DELETE /api/v1/users/me` implements the right to erasure: it needs the current password (a stolen token alone is not
enough), deletes the user and everything that references it with `ON DELETE CASCADE`, and ends its sessions. The last
administrator cannot delete their own account.

`GET /api/v1/users/me/export` implements the rights of access and portability. It returns the profile plus one section
per `PersonalDataProvider` bean, as a `personal-data.json` attachment; the template contributes the active sessions.
When you add a feature that stores user data, implement `PersonalDataProvider` and reference `users(id)` with
`ON DELETE CASCADE`, see [Using the template](../new-project/).

Audit log entries keep the user id after deletion. Decide how long you retain them and state it in your privacy policy.

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

## Security headers

Every response carries, on top of Spring Security's defaults (`X-Content-Type-Options: nosniff`,
`X-Frame-Options: DENY`, `Cache-Control: no-store`, and `Strict-Transport-Security` on HTTPS requests):

| Header | Value |
|--------|-------|
| `Content-Security-Policy` | `default-src 'none'; frame-ancestors 'none'; …` (with the sources Swagger UI needs) |
| `Referrer-Policy` | `no-referrer` |
| `Permissions-Policy` | camera, microphone, geolocation, payment and USB disabled |

They are set in `SecurityConfig`. If you serve HTML from the app, extend the policy there.

## Audit log

Security events are written to the `audit` logger and counted in the `security_audit_events_total` metric:

| Event | When |
|-------|------|
| `auth.login_succeeded`, `auth.login_failed`, `auth.login_blocked`, `auth.login_unverified` | Login attempts |
| `auth.logout`, `auth.sessions_revoked`, `auth.refresh_token_reused` | Session changes |
| `user.registered`, `user.email_verified`, `user.password_changed` | Account changes |
| `user.password_reset_requested`, `user.password_reset_completed` | Password reset |
| `user.account_deleted`, `user.data_exported` | Personal data requests |
| `admin.user_updated`, `admin.role_changed`, `admin.user_deleted` | Administrator actions |

Each entry has the affected `userId`, the authenticated `actorId`, the `clientIp`, and event-specific fields. Failed
logins include the `identifier` that was tried. With the prod profile these are separate JSON fields, for example:

```json
{"log":{"level":"WARN","logger":"audit"},"message":"event=auth.login_failed clientIp=203.0.113.7 identifier=alice",
 "event":"auth.login_failed","clientIp":"203.0.113.7","identifier":"alice","traceId":"…"}
```

Entries are written after the transaction commits, so rolled-back actions never appear. Add an `AuditEvent` and call
`AuditLog.record(...)` for your own sensitive actions, and ship the `audit` logger to long-term storage.

## Redis outages

The lockout, rate limit and revocation checks fail open: if Redis is unreachable, requests are allowed and a warning
is logged. That keeps the API available, at the cost of briefly disabling these protections. Refresh tokens live in
PostgreSQL and keep working. See [Production](../production/).
