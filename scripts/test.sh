#!/usr/bin/env bash
# Runs unit and integration tests (integration tests start PostgreSQL with Testcontainers).
# Usage: scripts/test.sh [maven args...]   e.g. scripts/test.sh -Dtest=UserServiceTest
source "$(dirname "$0")/lib.sh"
require_jdk
configure_testcontainers

./mvnw -B verify "$@"
info "Coverage report: target/site/jacoco/index.html"
