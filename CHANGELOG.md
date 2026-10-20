# Changelog

Notable changes to this template. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and
releases use [semantic versioning](https://semver.org/). Each release's full list of pull requests is in its
[GitHub release notes](https://github.com/hikmethankolay/spring-boot-backend-template/releases).

## [Unreleased]

## [2.8.2] - 2026-10-20

### Fixed

- Docker build failed on the Maven wrapper checksum (release 2.8.1 published no image; use 2.8.2).
- Account-takeover, session and lockout races found in a security audit.
- Lockout, startup, rate limit, release and logging follow-ups from the same audit. Rate-limit counters reset once
  on deploy because their Redis keys changed format.

[Unreleased]: https://github.com/hikmethankolay/spring-boot-backend-template/compare/v2.8.2...HEAD
[2.8.2]: https://github.com/hikmethankolay/spring-boot-backend-template/releases/tag/v2.8.2
