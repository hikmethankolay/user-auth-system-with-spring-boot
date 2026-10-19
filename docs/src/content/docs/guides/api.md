---
title: API overview
description: Endpoints at a glance, error format, pagination and response headers.
---

The full, generated reference with request and response schemas is the [REST API reference](../../api/). This page
is the short version.

## Endpoints

| Method | Path | Access | Success |
|--------|------|--------|---------|
| `POST` | `/api/v1/auth/register` | public | `201` + `Location`, sends a verification email |
| `POST` | `/api/v1/auth/login` | public | `200` access + refresh token, cookies |
| `POST` | `/api/v1/auth/refresh` | refresh token | `200` new tokens; the refresh token is single-use |
| `POST` | `/api/v1/auth/logout` | public | `204`; revokes the tokens, clears the cookies |
| `POST` | `/api/v1/auth/verify-email` | public | `204` |
| `POST` | `/api/v1/auth/verify-email/resend` | public | `202` |
| `POST` | `/api/v1/auth/password-reset` | public | `202` |
| `POST` | `/api/v1/auth/password-reset/confirm` | public | `204` |
| `GET` / `PATCH` | `/api/v1/users/me` | authenticated | `200` |
| `PUT` | `/api/v1/users/me/password` | authenticated | `204`; ends every session |
| `DELETE` | `/api/v1/users/me` | authenticated + password | `204` |
| `GET` | `/api/v1/users/me/export` | authenticated | `200` JSON attachment |
| `GET` | `/api/v1/users?page=0&size=20&sort=createdAt,desc` | admin | `200` page |
| `GET` | `/api/v1/users/{id}` | admin or that user | `200` |
| `PATCH` / `DELETE` | `/api/v1/users/{id}` | admin | `200` / `204` |

## Errors

Every error, including `401`, `403` and `429`, is an [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) problem
detail with the content type `application/problem+json`:

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

## Pagination

List endpoints take Spring Data's `page`, `size` (at most 100) and `sort` parameters and return:

```json
{ "content": [ ... ], "page": { "size": 20, "number": 0, "totalElements": 42, "totalPages": 3 } }
```

## Response headers

| Header | Meaning |
|--------|---------|
| `X-Trace-Id` | Trace id of the request, also present in the server logs |
| `RateLimit-Limit` / `RateLimit-Remaining` / `RateLimit-Reset` | Rate limit state (seconds until reset) |
| `Retry-After` | On `429`: seconds to wait |
| `Location` | On `201`: URL of the created resource |
| `Cache-Control: no-store` | On every response, including token responses |
| `Content-Security-Policy`, `Referrer-Policy`, `Permissions-Policy` | Browser hardening, see [Security](../security/#security-headers) |

## Token response

```json
{ "accessToken": "eyJ…", "tokenType": "Bearer", "expiresIn": 900, "refreshToken": "q3V…" }
```

`expiresIn` is the access token lifetime in seconds. Exchange the refresh token at `/api/v1/auth/refresh` before or
after the access token expires; each refresh token works once.
