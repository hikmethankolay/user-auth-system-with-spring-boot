---
title: Testing
description: Unit, integration, architecture and load tests.
---

```bash
scripts/test.sh            # unit, integration and ArchUnit tests; JaCoCo report in target/site/jacoco
scripts/docs.sh --strict   # fails on any Doxygen warning, including undocumented code
scripts/load-test.sh       # k6: fails if p95 > 300 ms or more than 1% of requests fail
```

## Integration tests

Integration tests extend `IntegrationTest`, which starts the full application against one throwaway
`postgres:18-alpine` and one `redis:8-alpine` container (Testcontainers), shared by all test classes. Outgoing mail is
captured in memory (`TestMailConfiguration`), so tests can follow the real verification and reset links.
Without a container runtime the integration tests are skipped, not failed.

```java
class OrderIntegrationTest extends IntegrationTest {

    @Test
    void createsOrder() throws Exception {
        String token = registerAndLogin(uniqueUsername());
        mockMvc.perform(post("/api/v1/orders").with(bearer(token)) ...)
               .andExpect(status().isCreated());
    }
}
```

## Architecture tests

`ArchitectureTest` enforces the layering rules on every build. See [Using the template](../new-project/#rules-the-build-enforces).

## Load test

`load-tests/api.js` has an authenticated user read `/api/v1/users/me`. All requests come from one IP, so start the
target with `RATE_LIMIT_ENABLED=false`:

```bash
RATE_LIMIT_ENABLED=false scripts/dev.sh                          # terminal 1
VUS=100 DURATION=20s THINK_TIME=0 scripts/load-test.sh           # terminal 2
```

`THINK_TIME` (default `0.1` s) is the pause between a virtual user's requests; `0` measures maximum throughput.
On a 20-core developer machine, one instance served about **7,500 req/s at a p95 of 19 ms**, with k6 running on the
same machine.

## Continuous integration

`.github/workflows/ci.yml` runs on pushes to `main` and on pull requests:

1. It runs the tests.
2. It builds the Docker image and scans it with Trivy, failing on fixable critical or high vulnerabilities.
3. It checks the documentation.

Dependabot opens weekly update PRs for Maven, GitHub Actions, Docker and npm.
