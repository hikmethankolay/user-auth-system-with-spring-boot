---
title: 'ADR 2: Redis-backed protections fail open'
description: Why lockout, rate limiting and token revocation allow requests when Redis is down.
---

**Status:** accepted

## Context

Login lockout, the per-IP rate limit and the access-token denylist live in Redis. Redis can be unavailable while
PostgreSQL and the application are healthy.

## Decision

These checks fail open: when Redis does not answer within its short timeout (500 ms), the request is allowed and a
warning is logged. Redis runs with `noeviction`, so counters and revocations are never silently dropped while it is up.

## Consequences

- A Redis outage keeps the API available instead of turning into a full outage.
- During the outage, brute-force lockout and rate limiting are off, and a revoked access token is accepted until it
  expires (at most `JWT_ACCESS_TOKEN_TTL`). Refresh tokens are in PostgreSQL and stay revocable.
- The `RedisDown` runbook covers what to watch for while it lasts.
