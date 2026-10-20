#!/usr/bin/env bash
# Builds the documentation website into target/pages with Astro Starlight (docs/):
#   guides       Markdown pages in docs/src/content/docs
#   api/         REST API reference (Redoc) from the OpenAPI document exported by OpenApiSpecExportIntegrationTest
#   reference/   Java reference (Doxygen)
#   coverage/    test coverage (JaCoCo)
# .github/workflows/pages.yml deploys it to GitHub Pages on every v* tag.
# Needs Node.js 22.22+ in addition to the JDK. Preview with: (cd docs && npm run preview)
#
# Usage: scripts/site.sh [--skip-build]   (--skip-build reuses existing test and Doxygen output)
source "$(dirname "$0")/lib.sh"

command -v npm >/dev/null 2>&1 || die "Node.js 22.22+ with npm is required to build the documentation site (https://nodejs.org)."

if [[ "${1:-}" != "--skip-build" ]]; then
  require_jdk
  configure_testcontainers
  ./mvnw -B verify
  "$ROOT_DIR/scripts/docs.sh" --strict
fi

[[ -f target/openapi.json ]] || die "target/openapi.json missing; it is written by the integration tests, which need Docker or Podman."
[[ -f target/doxygen/html/index.html ]] || die "Doxygen output missing; run scripts/docs.sh."
[[ -f target/site/jacoco/jacoco.xml ]] || die "JaCoCo report missing; run scripts/test.sh."

# Generated references are served as static files from docs/public (git-ignored).
rm -rf docs/public/reference docs/public/coverage
mkdir -p docs/public/reference docs/public/coverage
cp target/openapi.json docs/public/api/openapi.json
cp -R target/doxygen/html/. docs/public/reference/
cp -R target/site/jacoco/. docs/public/coverage/

# Build information shown on the home page.
export PUBLIC_SITE_VERSION="$(git describe --tags --abbrev=0 2>/dev/null || echo "dev")"
export PUBLIC_SITE_COMMIT="$(git rev-parse --short HEAD)"
export PUBLIC_SITE_DATE="$(date -u +%Y-%m-%d)"
# The last LINE counter in jacoco.xml is the total for the whole report.
export PUBLIC_SITE_COVERAGE="$(grep -o '<counter type="LINE" missed="[0-9]*" covered="[0-9]*"/>' target/site/jacoco/jacoco.xml | tail -n 1 \
  | sed -E 's/.*missed="([0-9]+)" covered="([0-9]+)".*/\1 \2/' | awk '{ printf "%.1f%%", 100 * $2 / ($1 + $2) }')"

info "Building the Starlight site"
(cd docs && npm ci --no-audit --no-fund && npm run build)

rm -rf target/pages
cp -R docs/dist target/pages
touch target/pages/.nojekyll   # serve directories starting with "_" (Astro, Doxygen) as they are

info "Site for $PUBLIC_SITE_VERSION ($PUBLIC_SITE_COMMIT, coverage $PUBLIC_SITE_COVERAGE): target/pages"
