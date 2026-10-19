#!/usr/bin/env bash
# Generates the Doxygen HTML documentation into target/doxygen/html.
# Usage: scripts/docs.sh [--strict]   (--strict fails on any Doxygen warning, including undocumented code)
source "$(dirname "$0")/lib.sh"

command -v doxygen >/dev/null 2>&1 \
  || die "Doxygen not found. Install it, e.g. 'sudo dnf install doxygen' or 'sudo apt install doxygen'."

mkdir -p target/doxygen
if [[ "${1:-}" == "--strict" ]]; then
  { cat Doxyfile; echo "WARN_AS_ERROR = FAIL_ON_WARNINGS"; } | doxygen - \
    || die "Documentation warnings found:"$'\n'"$(cat target/doxygen/warnings.log)"
else
  doxygen Doxyfile
  [[ -s target/doxygen/warnings.log ]] && warn "Doxygen warnings: target/doxygen/warnings.log"
fi

info "Documentation: target/doxygen/html/index.html"
