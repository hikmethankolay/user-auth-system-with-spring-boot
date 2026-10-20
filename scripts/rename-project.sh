#!/usr/bin/env bash
# Turns this template into your own project: renames the Maven coordinates, the application
# name and the base Java package. Run it once, right after copying the template.
#
# Usage: scripts/rename-project.sh <groupId> <artifactId> [basePackage]
# Example: scripts/rename-project.sh com.acme order-service com.acme.orders
source "$(dirname "$0")/lib.sh"

old_group="com.hikmethankolay"
old_artifact="spring-boot-template"
old_package="com.hikmethankolay.template"

[[ $# -ge 2 ]] || die "Usage: scripts/rename-project.sh <groupId> <artifactId> [basePackage]"
new_group="$1"
new_artifact="$2"
new_package="${3:-$new_group.$(echo "$new_artifact" | tr -cd 'a-zA-Z0-9' | tr '[:upper:]' '[:lower:]')}"

java_name='[a-z_][a-z0-9_]*'
[[ "$new_group" =~ ^$java_name(\.$java_name)*$ ]] || die "Invalid groupId '$new_group' (e.g. com.acme)."
[[ "$new_artifact" =~ ^[a-z][a-z0-9-]*$ ]] || die "Invalid artifactId '$new_artifact' (lowercase, digits, '-')."
[[ "$new_package" =~ ^$java_name(\.$java_name)*$ ]] || die "Invalid package '$new_package' (e.g. com.acme.orders)."
grep -q "<artifactId>$old_artifact</artifactId>" pom.xml || die "Project was already renamed."

info "Renaming $old_group:$old_artifact ($old_package) -> $new_group:$new_artifact ($new_package)"

# Maven coordinates. The parent's groupId is org.springframework.boot, so the replacement is unambiguous.
sed_inplace -e "s|<groupId>$old_group</groupId>|<groupId>$new_group</groupId>|" pom.xml

# Application/artifact name everywhere it appears in tracked files.
grep -rlI --exclude-dir=.git --exclude-dir=target --exclude-dir=node_modules --exclude-dir=dist --exclude-dir=.astro \
  --exclude=rename-project.sh --exclude=package-lock.json "$old_artifact" . | while read -r file; do
  sed_inplace "s|$old_artifact|$new_artifact|g" "$file"
done

# Java packages: move the source trees, then rewrite package and import statements.
old_path="${old_package//.//}"
new_path="${new_package//.//}"
for root in src/main/java src/test/java; do
  [[ -d "$root/$old_path" ]] || continue
  mkdir -p "$root/$new_path"
  tmp_dir="$(mktemp -d)"
  mv "$root/$old_path"/* "$tmp_dir"/
  mv "$tmp_dir"/* "$root/$new_path"/
  rmdir "$tmp_dir"
  # Remove now-empty parent directories of the old package.
  find "$root" -type d -empty -delete
done
grep -rlI --exclude-dir=.git --exclude-dir=target --exclude-dir=node_modules --exclude-dir=dist --exclude-dir=.astro \
  --exclude=rename-project.sh --exclude=package-lock.json "$old_package" . | while read -r file; do
  sed_inplace "s|$old_package|$new_package|g" "$file"
done

# The template's release history does not apply to the new project.
cat > CHANGELOG.md <<'EOF2'
# Changelog

Notable changes to this project. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]
EOF2

info "Done. Review the changes (git status), then run scripts/test.sh."
