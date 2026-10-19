#!/usr/bin/env bash
# Manages the local PostgreSQL container.
# Usage: scripts/db.sh up|down|reset|logs|psql|status
source "$(dirname "$0")/lib.sh"
detect_compose

case "${1:-up}" in
  up)
    info "Starting PostgreSQL"
    "${COMPOSE[@]}" up -d --wait db
    ;;
  down)
    "${COMPOSE[@]}" stop db
    ;;
  reset)
    read -r -p "This deletes all local database data. Continue? [y/N] " answer
    [[ "$answer" =~ ^[Yy]$ ]] || exit 0
    "${COMPOSE[@]}" down -v
    "${COMPOSE[@]}" up -d --wait db
    ;;
  logs)
    "${COMPOSE[@]}" logs -f db
    ;;
  psql)
    "${COMPOSE[@]}" exec db sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
    ;;
  status)
    "${COMPOSE[@]}" ps db
    ;;
  *)
    die "Unknown command '$1'. Use: up|down|reset|logs|psql|status"
    ;;
esac
