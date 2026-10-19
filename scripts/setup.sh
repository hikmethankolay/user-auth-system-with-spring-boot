#!/usr/bin/env bash
# One-time setup: checks prerequisites, creates .env with generated secrets and compiles the project.
# Usage: scripts/setup.sh [--force]   (--force recreates .env)
source "$(dirname "$0")/lib.sh"

force=false
[[ "${1:-}" == "--force" ]] && force=true

random_secret() {
  if command -v openssl >/dev/null 2>&1; then
    openssl rand -base64 "$1" | tr -d '\n/+='
  else
    head -c "$1" /dev/urandom | base64 | tr -d '\n/+='
  fi
}

info "Checking prerequisites"
require_jdk
if (detect_compose) 2>/dev/null; then
  info "Container runtime found"
else
  warn "Docker or Podman not found: scripts/db.sh, scripts/docker.sh and integration tests need one."
fi

if [[ -f .env && "$force" == false ]]; then
  info ".env already exists (use --force to recreate it)"
else
  info "Creating .env from .env.example"
  cp .env.example .env
  admin_password="Admin#$(random_secret 12)"
  sed_inplace "s|^JWT_SECRET=.*|JWT_SECRET=$(random_secret 48)|" .env
  sed_inplace "s|^ADMIN_PASSWORD=.*|ADMIN_PASSWORD=${admin_password}|" .env
  info "Generated JWT_SECRET and an initial admin password: ${admin_password}"
  info "(stored in .env; change it after the first login)"
fi

chmod +x mvnw scripts/*.sh

info "Compiling (downloads dependencies on first run)"
./mvnw -B -q -DskipTests compile

info "Done. Next: scripts/dev.sh to start the database and the app."
