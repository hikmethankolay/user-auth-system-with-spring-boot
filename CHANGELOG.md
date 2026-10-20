# Changelog

Notable changes to this template. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and
releases use [semantic versioning](https://semver.org/). Each release's full list of pull requests is in its
[GitHub release notes](https://github.com/hikmethankolay/spring-boot-backend-template/releases).

## [Unreleased]

## [2.9.0] - 2026-10-21

### Added

- `JWT_ALGORITHM=RS256` signs with `JWT_PRIVATE_KEY` and publishes the public keys at `/.well-known/jwks.json`.
- Key rotation: `JWT_SECRET` and `JWT_PRIVATE_KEY` take a comma-separated list (the first signs, all verify), and
  tokens carry a `kid`. Tokens issued by 2.8 still verify.
- SLOs with burn-rate alerts, log-based alerts, a scheduled-job alert and a runbook for every alert.
- Build gates: Maven Enforcer, Error Prone, SpotBugs with FindSecBugs, Spotless (palantir-java-format) and JaCoCo
  coverage minimums. PIT mutation testing of `security` and `auth` runs weekly with a 95% threshold.
- CodeQL and OpenSSF Scorecard workflows; the release verifies its own signature and provenance and attaches the SBOM.
- `scripts/test.sh --unit` for the unit tests alone; integration tests are named `*IntegrationTest` and run in Failsafe.
- Architecture decision records, a compatibility table, a code of conduct and this changelog.

### Fixed

- Audit log key-value fields could carry line breaks into text log layouts.

## [2.8.2] - 2026-10-20

### Fixed

- Docker build failed on the Maven wrapper checksum (release 2.8.1 published no image; use 2.8.2).
- Account-takeover, session and lockout races found in a security audit.
- Lockout, startup, rate limit, release and logging follow-ups from the same audit. Rate-limit counters reset once
  on deploy because their Redis keys changed format.

[Unreleased]: https://github.com/hikmethankolay/spring-boot-backend-template/compare/v2.9.0...HEAD
[2.9.0]: https://github.com/hikmethankolay/spring-boot-backend-template/compare/v2.8.2...v2.9.0
[2.8.2]: https://github.com/hikmethankolay/spring-boot-backend-template/releases/tag/v2.8.2
