---
title: 'ADR 5: Transactions belong to services, not controllers'
description: Why controllers are never transactional and open-in-view is off.
---

**Status:** accepted

## Context

A transaction that spans a whole request holds a database connection while JSON is written to the client, and lets
lazy loading run queries from the web layer.

## Decision

`@Transactional` goes on service methods only. Controllers call services and map their results; they never use
repositories (an ArchUnit rule enforces it). `spring.jpa.open-in-view` is `false`.

## Consequences

- Connections are held only for the database work, which keeps the pool small under load.
- Services return fully loaded DTOs; touching a lazy association outside a service fails fast instead of issuing a
  hidden query.
- Side effects that must follow a commit (audit log, emails) are registered with the transaction, not run inline.
