#!/usr/bin/env bash
# Runs the k6 load test (load-tests/api.js) against a running app, using the k6 container image,
# so no local k6 install is needed. All requests come from one IP, so start the target app with
# RATE_LIMIT_ENABLED=false (and REQUIRE_VERIFIED_EMAIL=false), e.g.:
#   RATE_LIMIT_ENABLED=false scripts/dev.sh
#
# Usage: scripts/load-test.sh [base-url]   (default http://localhost:8080)
# Env:   VUS (virtual users, default 20), DURATION (default 30s)
# On macOS/Windows with Docker Desktop use http://host.docker.internal:8080 as the base URL.
source "$(dirname "$0")/lib.sh"
detect_container_cli

base_url="${1:-http://localhost:${PORT:-8080}}"
volume_options="ro"
[[ "${CONTAINER[0]}" == "podman" ]] && volume_options="ro,Z"

info "Load testing $base_url with ${VUS:-20} virtual users for ${DURATION:-30s}"
"${CONTAINER[@]}" run --rm --network host \
  -v "$ROOT_DIR/load-tests:/scripts:$volume_options" \
  -e BASE_URL="$base_url" -e VUS="${VUS:-20}" -e DURATION="${DURATION:-30s}" \
  docker.io/grafana/k6:1.4.0 run /scripts/api.js
