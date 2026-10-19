---
title: Getting started
description: Run the template locally in three commands.
---

## Prerequisites

- **JDK 25.** A JRE is not enough, because Maven needs `javac`. For example `sudo dnf install java-25-openjdk-devel`,
  `sudo apt install openjdk-25-jdk`, or `winget install EclipseAdoptium.Temurin.25.JDK` on Windows.
- **Docker or Podman**, for PostgreSQL, Redis and Mailpit in development and for the integration tests.

## Run it

```bash
git clone https://github.com/hikmethankolay/user-auth-system-with-spring-boot.git
cd user-auth-system-with-spring-boot

scripts/setup.sh    # checks prerequisites, creates .env with generated secrets, compiles
scripts/dev.sh      # starts PostgreSQL, Redis and Mailpit, then the app on http://localhost:8080
scripts/smoke.sh    # in another terminal: register, login, /me, refresh, logout against the running app
```

On Windows use the same names with `.bat`: `scripts\setup.bat`, `scripts\dev.bat`, `scripts\smoke.bat`.

## Where to look

| What | URL |
|------|-----|
| API | http://localhost:8080/api/v1 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Emails sent in development | http://localhost:8025 (Mailpit) |
| Health | http://localhost:8080/actuator/health |

`setup` prints the generated password of the initial admin user `admin`, which is created on first start and stored
in `.env`.

## Try the API

```bash
curl -i -X POST localhost:8080/api/v1/auth/register -H 'Content-Type: application/json' \
  -d '{"username":"alice","email":"alice@example.com","password":"Secret#123"}'

TOKEN=$(curl -s -X POST localhost:8080/api/v1/auth/login -H 'Content-Type: application/json' \
  -d '{"identifier":"alice","password":"Secret#123"}' | sed -E 's/.*"accessToken":"([^"]+)".*/\1/')

curl -s localhost:8080/api/v1/users/me -H "Authorization: Bearer $TOKEN"
```

Registering sends a verification email to Mailpit. In development, login works before you verify. In the
[production profile](../production/) it doesn't.

## Stop it

Press **Ctrl+C** to stop the app, then `scripts/db.sh down` to stop the containers. Data is kept until you run
`scripts/db.sh reset`.
