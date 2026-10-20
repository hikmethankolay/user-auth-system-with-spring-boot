---
title: 'ADR 1: HS256 by default, RS256 when other services verify tokens'
description: Why access tokens are HMAC-signed by default and when to switch to RSA.
---

**Status:** accepted

## Context

Access tokens are JWTs checked on every request. A template is most often used for one backend that both issues and
checks its tokens, but some projects grow a second service that must accept them.

## Decision

`HS256` with `JWT_SECRET` is the default: one secret, nothing to publish, the fastest verification. `RS256` with
`JWT_PRIVATE_KEY` is one setting away (`JWT_ALGORITHM=RS256`) and publishes the public keys at
`/.well-known/jwks.json`. Both take a list of keys so a key can be rotated without logging anyone out.

## Consequences

- With `HS256`, any service that can verify tokens can also forge them, so the secret must never leave this service.
- Switching to `RS256` invalidates the access tokens in flight (at most 15 minutes of them); refresh tokens keep
  working, so clients recover on their own.
- See [Signing keys](../../guides/security/#signing-keys).
