#!/usr/bin/env bash
# Runs the jar built by scripts/build.sh. Configuration is read from .env and the environment.
# Usage: scripts/run.sh [app args...]
source "$(dirname "$0")/lib.sh"
require_env

command -v java >/dev/null 2>&1 || die "Java 25+ runtime not found."
jar="$(ls target/*.jar 2>/dev/null | grep -v '\.original$' | head -n 1 || true)"
[[ -n "$jar" ]] || die "No jar in target/. Run scripts/build.sh first."

info "Running $jar"
exec java -jar "$jar" "$@"
