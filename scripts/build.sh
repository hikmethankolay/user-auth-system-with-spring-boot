#!/usr/bin/env bash
# Builds the executable jar into target/. Tests run unless --skip-tests is given.
# Usage: scripts/build.sh [--skip-tests]
source "$(dirname "$0")/lib.sh"
require_jdk

if [[ "${1:-}" == "--skip-tests" ]]; then
  ./mvnw -B clean package -DskipTests
else
  configure_testcontainers
  ./mvnw -B clean verify
fi

info "Built: $(ls target/*.jar | grep -v '\.original$')"
