#!/usr/bin/env bash
# Runs the whole stack (PostgreSQL + app image) with Docker/Podman Compose.
# Usage: scripts/docker.sh up|down|logs|ps
source "$(dirname "$0")/lib.sh"
require_env
detect_compose

case "${1:-up}" in
  up)
    info "Building the image and starting the stack"
    "${COMPOSE[@]}" --profile app up -d --build --wait
    info "App running on http://localhost:${PORT:-8080}"
    ;;
  down)
    "${COMPOSE[@]}" --profile app down
    ;;
  logs)
    "${COMPOSE[@]}" --profile app logs -f app
    ;;
  ps)
    "${COMPOSE[@]}" --profile app ps
    ;;
  *)
    die "Unknown command '$1'. Use: up|down|logs|ps"
    ;;
esac
