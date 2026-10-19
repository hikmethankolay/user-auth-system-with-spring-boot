---
title: Observability
description: Metrics, tracing and logs.
---

## Metrics

Prometheus metrics are served at `/actuator/prometheus`, tagged with `application`. In the prod profile Actuator runs
on the internal port `8081`. Point Prometheus at that port and keep it off the public network.

## Tracing

Requests are traced with OpenTelemetry.

- **Logs:** trace and span ids appear in every log line.
- **Responses:** every response carries the trace id in `X-Trace-Id`, so a client's error report leads straight to the
  matching logs.
- **Sampling:** `TRACING_SAMPLING_PROBABILITY` sets the share of requests traced (`1.0` locally, `0.1` in prod).
- **Export:** traces are exported only once an OTLP endpoint is configured, for example
  `MANAGEMENT_OPENTELEMETRY_TRACING_EXPORT_OTLP_ENDPOINT=http://otel-collector:4318/v1/traces` for Jaeger, Tempo or any
  OpenTelemetry collector.

## Logs

Logs are readable text locally. In the prod profile, each line is one JSON object in the Elastic Common Schema, ready
for Elasticsearch, Loki or any log pipeline.

```json
{"@timestamp":"2026-10-19T21:50:30.821Z","log":{"level":"INFO","logger":"com.hikmethankolay.template.Application"},
 "service":{"name":"spring-boot-template"},"message":"Started Application in 4.1 seconds"}
```

## Health

`/actuator/health`, with Kubernetes-style `liveness` and `readiness` groups. Redis and mail are excluded on purpose,
because the app keeps working without them.
