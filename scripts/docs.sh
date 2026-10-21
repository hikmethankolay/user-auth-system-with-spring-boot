#!/usr/bin/env bash
# Generates the Java reference (Javadoc) into target/reports/apidocs. Fails on any Javadoc warning,
# including undocumented public, protected and package-private code.
# Usage: scripts/docs.sh
source "$(dirname "$0")/lib.sh"
require_jdk

./mvnw -B -q javadoc:javadoc
info "Documentation: target/reports/apidocs/index.html"
