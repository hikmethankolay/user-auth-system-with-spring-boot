#!/usr/bin/env bash
# Starts PostgreSQL and runs the app from source (Ctrl+C to stop). Extra arguments go to Maven.
# Usage: scripts/dev.sh [maven args...]
source "$(dirname "$0")/lib.sh"
require_jdk
require_env

"$ROOT_DIR/scripts/db.sh" up

info "Starting the application on http://localhost:${PORT:-8080} (Swagger UI: /swagger-ui.html)"
./mvnw -B spring-boot:run "$@"
