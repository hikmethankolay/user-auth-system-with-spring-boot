---
title: 'ADR 3: Emails go through a transactional outbox'
description: Why the application never sends email from the request.
---

**Status:** accepted

## Context

Verification and password-reset emails are triggered by database changes. Sending them from the request couples the
response time to the SMTP server, and an email can be sent for a change that later rolls back, or lost when the
process stops between commit and send.

## Decision

Emails are written to the `email_outbox` table in the same transaction as the change. A scheduled dispatcher sends
them with retries and backoff, and gives up after a fixed number of attempts.

## Consequences

- An email is sent only if its change committed, and survives restarts and SMTP outages.
- Delivery is asynchronous (seconds, not milliseconds), and needs monitoring: `mail_outbox_pending` and
  `mail_outbox_dropped_total` have alerts.
