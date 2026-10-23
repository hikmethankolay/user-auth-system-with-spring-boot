#!/usr/bin/env bash
# Generates TypeScript types for the API from the committed OpenAPI document (api/openapi.json) with
# openapi-typescript. Pair them with openapi-fetch for a typed client:
#   import createClient from "openapi-fetch";
#   import type { paths } from "./api";
#   const api = createClient<paths>({ baseUrl: "http://localhost:8080", credentials: "include" });
#   const { data, error } = await api.GET("/api/v1/users/me");
# Needs Node.js with npm. Run it again after regenerating api/openapi.json.
#
# Usage: scripts/client.sh [output-file]   (default: target/client/api.ts)
#   e.g. scripts/client.sh ../web/src/api.ts
source "$(dirname "$0")/lib.sh"

command -v npx >/dev/null 2>&1 || die "Node.js with npm is required to generate the client (https://nodejs.org)."

output="${1:-target/client/api.ts}"
mkdir -p "$(dirname "$output")"
npx --yes openapi-typescript@7.13.0 api/openapi.json --output "$output"
info "TypeScript types: $output (use them with openapi-fetch)"
