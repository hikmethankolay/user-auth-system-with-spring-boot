#!/usr/bin/env bash
# Generates an API client from the committed OpenAPI document (api/openapi.json) with OpenAPI Generator, run in its
# container image (needs Docker or Podman, no Java or Node.js). Run it again after regenerating api/openapi.json.
#
# The default, typescript-fetch, is a TypeScript client on the browser's fetch, without runtime dependencies:
#   import { Configuration, UsersApi } from "./api";
#   const config = new Configuration({ basePath: "http://localhost:8080", credentials: "include" });
#   const me = await new UsersApi(config).getCurrentUser();
# --generator picks another language (kotlin, swift5, dart, java, python, ...):
# https://openapi-generator.tech/docs/generators
#
# Usage: scripts/client.sh [--generator <name>] [output-dir]   (default: typescript-fetch, target/client/<name>)
#   e.g. scripts/client.sh ../web/src/api
#        scripts/client.sh --generator kotlin ../android/api
source "$(dirname "$0")/lib.sh"

generator="typescript-fetch"
if [[ "${1:-}" == "--generator" ]]; then
  [[ -n "${2:-}" ]] || die "Usage: scripts/client.sh [--generator <name>] [output-dir]"
  generator="$2"
  shift 2
fi
detect_container_cli

output="${1:-target/client/$generator}"
mkdir -p "$output"
output="$(cd "$output" && pwd)"
spec_mount="$ROOT_DIR/api:/spec:ro"
out_mount="$output:/out"
user_options=(--user "$(id -u):$(id -g)")
# Rootless Podman already writes as the host user, and needs the SELinux label to mount the directories.
if [[ "${CONTAINER[0]}" == "podman" ]]; then
  spec_mount+=",Z"
  out_mount+=":Z"
  user_options=()
fi
"${CONTAINER[@]}" run --rm ${user_options[@]+"${user_options[@]}"} -v "$spec_mount" -v "$out_mount" \
  docker.io/openapitools/openapi-generator-cli:v7.26.0 generate -i /spec/openapi.json -g "$generator" -o /out
info "$generator client: $output"
