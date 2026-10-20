---
title: API overview
description: Endpoints at a glance, error format, pagination and response headers.
---

The full, generated reference with request and response schemas is the [REST API reference](../../api/). This page
is the short version.

## OpenAPI document

The application generates its OpenAPI 3.1 document from the controllers (springdoc) and serves it at `/v3/api-docs`
(Swagger UI at `/swagger-ui.html`, both off in the prod profile). The same document is committed as
[`api/openapi.json`](https://github.com/hikmethankolay/spring-boot-backend-template/blob/main/api/openapi.json), so
API changes show up as diffs in pull requests and client generators can use it without running the app.

The document describes the behaviour, not just the shapes:

- every operation has a specific `operationId` (`registerUser`, `listUsers`, `getCurrentUser`, …), so generated
  clients get usable method names;
- error responses are `application/problem+json` with the `Problem` schema: 400 on operations with input, 401 on
  protected ones, 403 where a role is required, 404 on `/{id}`, 409 where a username or email can conflict, and 429
  everywhere;
- public operations have an empty `security` list; protected ones accept the `bearerAuth` header **or** the
  `cookieAuth` cookie (`auth_token`); refresh and logout also document the `refresh_token` cookie;
- responses declare `X-Trace-Id` and `RateLimit-*` (plus `Retry-After` on 429, `Location` on 201 and `Set-Cookie`
  on token responses);
- pagination is three query parameters, `page`, `size` (at most 100) and `sort`.

Two tests keep it honest. `OpenApiContractTest` checks these rules on the generated document, and
`OpenApiSpecExportTest` fails when `api/openapi.json` differs from it. After an intended API change, regenerate the
file and commit it with the code:

```bash
scripts/test.sh -Dtest=OpenApiSpecExportTest -Dopenapi.update=true
git diff api/openapi.json
```

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

Every email field (`register`, `PATCH /users/me`, the admin update and the email endpoints) uses the same rule,
`@ValidEmail`: a well-formed address of 3 to 254 characters. Tokens must not be empty.

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

List endpoints take Spring Data's `page`, `size` (at most 100) and `sort` parameters. `sort` only accepts the
properties listed in its parameter description; for `/api/v1/users` that is `id`, `username`, `email`, `role`,
`emailVerified`, `createdAt` and `updatedAt`. Anything else, including real but hidden columns such as the password
hash, is rejected with `400`, because sorting by a hidden column reveals information about its values. They return:

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
