#!/usr/bin/env bash
# Manages the local backing services: PostgreSQL and Redis.
# Usage: scripts/db.sh up|down|reset|logs|psql|redis-cli|status
source "$(dirname "$0")/lib.sh"
detect_compose

services=(db redis)

case "${1:-up}" in
  up)
    info "Starting PostgreSQL and Redis"
    "${COMPOSE[@]}" up -d --wait "${services[@]}"
    ;;
  down)
    "${COMPOSE[@]}" stop "${services[@]}"
    ;;
  reset)
    read -r -p "This deletes all local database and cache data. Continue? [y/N] " answer
    [[ "$answer" =~ ^[Yy]$ ]] || exit 0
    "${COMPOSE[@]}" down -v
    "${COMPOSE[@]}" up -d --wait "${services[@]}"
    ;;
  logs)
    "${COMPOSE[@]}" logs -f "${services[@]}"
    ;;
  psql)
    "${COMPOSE[@]}" exec db sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
    ;;
  redis-cli)
    "${COMPOSE[@]}" exec redis redis-cli
    ;;
  status)
    "${COMPOSE[@]}" ps "${services[@]}"
    ;;
  *)
    die "Unknown command '$1'. Use: up|down|reset|logs|psql|redis-cli|status"
    ;;
esac
