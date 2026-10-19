---
title: Scripts
description: One script per task, for Linux, macOS (.sh) and Windows (.bat).
---

Every script exists as `scripts/<name>.sh` and `scripts\<name>.bat`.

| Script | What it does |
|--------|--------------|
| `setup` | Checks JDK 25 and a container runtime, creates `.env` from `.env.example` with a random `JWT_SECRET` and admin password, compiles. `--force` recreates `.env`. |
| `dev` | Starts PostgreSQL, Redis and Mailpit, then runs the app from source with `spring-boot:run`. |
| `db` | Local services: `up`, `down`, `reset` (deletes data), `logs`, `psql`, `redis-cli`, `status`. |
| `test` | Unit, integration and architecture tests plus the coverage report (`target/site/jacoco/index.html`). Extra arguments go to Maven, e.g. `-Dtest=UserServiceTest`. |
| `build` | Builds `target/*.jar`; runs the tests unless `--skip-tests`. |
| `run` | Runs the built jar with the settings from `.env`. |
| `docker` | Whole stack in containers (PostgreSQL, Redis, Mailpit, app image): `up`, `down`, `logs`, `ps`. |
| `smoke` | End-to-end check against a running instance: `scripts/smoke.sh [base-url]`. |
| `load-test` | k6 load test against a running instance. See [Testing](../testing/#load-test). |
| `docs` | Doxygen HTML in `target/doxygen/html`. `--strict` fails on any Doxygen warning, including undocumented code. |
| `site` | Builds this documentation website into `target/pages`. |
| `rename-project` | Renames Maven coordinates, app name and Java package. See [Using the template](../new-project/). |

## Container runtimes

- **Linux:** `test` uses rootless Podman automatically when Docker isn't available. It starts `podman.socket` and points
  Testcontainers at it.
- **Windows:** the `.bat` scripts need Docker Desktop, or Podman with its Docker API enabled. `rename-project.bat` runs
  the shell script through Git Bash.
