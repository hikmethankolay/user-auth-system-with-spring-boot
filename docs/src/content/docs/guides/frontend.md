---
title: Connecting a frontend
description: The pages your frontend needs, cookie or Bearer tokens, the login steps, refreshing and errors.
---

The API has no pages of its own. Emails and the GitHub/Google redirect point to your frontend, which calls the API.
This page lists what the frontend has to do; [Security](../security/) explains why each piece works the way it does.

## Settings

| Variable | Locally | In production |
|----------|---------|---------------|
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Your frontend's origin, e.g. `https://app.example.com` |
| `MAIL_LINK_BASE_URL` | `http://localhost:3000` | Your frontend's URL; emails link to its `/verify-email` and `/reset-password` pages |
| `OAUTH2_REDIRECT_URI` | `http://localhost:3000/oauth2/callback` | Your callback page, registered at GitHub and Google |

With `SPRING_PROFILES_ACTIVE=prod` the app refuses to start while any of these still points to localhost.

## Cookies or Bearer tokens

Every login returns the tokens twice: in the body and as HttpOnly cookies (`auth_token`, and `refresh_token`, which
is only sent to `/api/v1/auth`). Pick one:

- **Cookies**, for a browser app on the same site as the API (`app.example.com` and `api.example.com`, or
  `localhost:3000` and `localhost:8080`). Call the API with `credentials: "include"` and ignore the tokens in the body:
  JavaScript never sees the cookies, so an XSS bug cannot steal them. The cookies are `SameSite=Strict`, so the
  browser does not send them from another site.
- **Bearer tokens**, for mobile and desktop apps, or a browser app on a different site. Send
  `Authorization: Bearer <accessToken>` and pass `refreshToken` in the body of `/refresh` and `/logout`. Keep the
  tokens in memory or in the platform's secure storage (Keychain, Keystore), not in `localStorage`.

## Typed client

```bash
scripts/client.sh ../web/src/api.ts
```

```ts
import createClient from "openapi-fetch";
import type { paths } from "./api";

export const api = createClient<paths>({ baseUrl: "http://localhost:8080", credentials: "include" });
```

Run the script again after every change to `api/openapi.json`; see [API overview](../api/#typescript-client).

## Pages to build

| Page | What it does |
|------|--------------|
| Sign up | `POST /api/v1/auth/register` with `username`, `email`, `password` (8–72 characters with upper and lower case, a digit and a symbol) |
| Log in | `POST /api/v1/auth/login` with `identifier` (username or email), `password` and optional `rememberMe`; see [Logging in](#logging-in) |
| `/verify-email?token=…` | `POST /api/v1/auth/verify-email` with `{"token": "…"}` from the URL; offer `POST /api/v1/auth/verify-email/resend` |
| Forgot password | `POST /api/v1/auth/password-reset` with `{"email": "…"}`; it answers `202` even for unknown addresses |
| `/reset-password?token=…` | `POST /api/v1/auth/password-reset/confirm` with `{"token": "…", "newPassword": "…"}`, then send the user to log in |
| `/oauth2/callback` | Only with GitHub or Google login: check `state`, then `POST /api/v1/auth/oauth2/{provider}` with `code` and `codeVerifier`; see [the steps](../security/#login-with-github-and-google) |
| Account settings | `GET`/`PATCH /api/v1/users/me`, `PUT /api/v1/users/me/password`, two-factor enrollment, `GET /api/v1/users/me/export`, `DELETE /api/v1/users/me` with `{"password": "…"}` |

A password change or reset ends every session, including the current one: send the user to the login page afterwards.

## Logging in

Login answers `200` with tokens, or `202` with an `mfaToken` when the user has two-factor authentication. GitHub and
Google login answer the same way.

```ts
const { data, error } = await api.POST("/api/v1/auth/login", {
  body: { identifier, password, rememberMe },
});
if (error) return showProblem(error);            // see Errors below
if ("mfaToken" in data) {                         // 202: the second step is pending
  const code = await askForCode();                // 6 digits from the app, or a recovery code
  const second = await api.POST("/api/v1/auth/login/mfa", {
    body: { mfaToken: data.mfaToken, code },     // valid for data.expiresIn seconds
  });
  if (second.error) return showProblem(second.error);
}
// Logged in: the cookies are set.
```

To turn two-factor authentication on, `POST /api/v1/users/me/mfa/totp` returns an `otpauth://` URI: show it as a QR
code (any QR library), then send the password and the first code to `/confirm` and show the ten recovery codes it
returns. They are shown only once.

## Refreshing

The access token expires after 15 minutes. When a call answers `401`, refresh once and retry; when the refresh
answers `401` as well, the session has ended and the user must log in again.

```ts
async function call<T>(request: () => Promise<{ data?: T; error?: unknown; response: Response }>) {
  let result = await request();
  if (result.response.status === 401 && (await refresh())) {
    result = await request();
  }
  return result;
}

// One refresh at a time across all tabs: a refresh token works once, and two tabs presenting the same token look
// like a stolen token, which ends the session.
function refresh(): Promise<boolean> {
  return navigator.locks.request("auth-refresh", async () =>
    (await api.POST("/api/v1/auth/refresh", {})).response.ok);
}
```

In Bearer mode, keep the new `accessToken` and `refreshToken` from each refresh response; the old refresh token no
longer works. Log out with `POST /api/v1/auth/logout`, which ends the session and clears the cookies.

## Errors

Every error is an [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) problem with a stable `type`; validation errors
add an `errors` list with one entry per field. Branch on `type`, never on `title` or `detail`:

| `type` | Show |
|--------|------|
| `urn:problem:validation-failed` | The message of each field in `errors` next to its input |
| `urn:problem:invalid-credentials` | "Wrong username or password" |
| `urn:problem:email-not-verified` | A hint to check the inbox, with the resend button |
| `urn:problem:invalid-mfa-code` / `mfa-challenge-expired` | Ask for the code again / restart the login |
| `urn:problem:login-locked` / `rate-limited` | Wait `Retry-After` seconds |
| `urn:problem:username-taken` / `email-taken` | Mark the field |

Every type is listed in [API overview](../api/#errors). Put the `X-Trace-Id` response header into error reports: it
finds the request in the server logs.
