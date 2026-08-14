---
number: 3
title: Adopt OpenTelemetry Collector and OpenObserve for observability
status: accepted
date: 2026-08-12
links:
  - target: 4
    kind: relatesto
---

# Adopt OpenTelemetry Collector and OpenObserve for observability

## Context and Problem Statement

NFR-09 requires unified logging, metrics, and tracing. The dev environment needs a lightweight
observability backend. Which observability backend should Dokja use, and how should applications
send telemetry to it?

## Decision Drivers

- NFR-09: unified logging, metrics, and tracing with request IDs and alerting.
- A single backend that ingests all three signals.
- Low ops overhead for a learning/demo project; trivial dev bring-up via docker compose.

## Considered Options

- OpenObserve with OpenTelemetry Collector gateway
- Prometheus + Grafana + Loki + Tempo
- Elastic Stack (Elasticsearch, Kibana, Beats/APM)
- SigNoz

## Decision Outcome

Chosen option: "OpenObserve with OpenTelemetry Collector gateway", because it is the only option
that covers logs, metrics, and traces in a single binary with a built-in UI while speaking native
OTLP. The OpenTelemetry Collector sits in front as a stable gateway, so applications export a
standard protocol and the backend stays swappable. This satisfies NFR-09 for development with just
two docker compose services and no extra infrastructure.

### Consequences

- Good, because one backend and one UI cover all three signals.
- Good, because applications export standard OTLP to the collector; the backend behind it can be
  replaced without touching application code.
- Neutral, because dev bring-up is two compose services.
- Bad, because OpenObserve is younger and less battle-tested than the ELK or Prometheus ecosystems.
- Bad, because its API surface evolves quickly (e.g. the `/otlp` path segment was removed in v0.92)
  and it enforces a strict root-password policy; images must stay pinned and credentials documented.

## Pros and Cons of the Options

### OpenObserve with OpenTelemetry Collector gateway

Collector receives OTLP from applications and forwards to a single-node OpenObserve instance.

- Good, because one binary + UI handles logs, metrics, and traces.
- Good, because OTLP/HTTP and OTLP/gRPC are supported natively.
- Good, because the collector decouples applications from the backend and adds batching/memory
  limiting.
- Good, because dev setup is minimal.
- Bad, because OpenObserve's API and password-policy changes between minor versions require pinned
  image versions and documented credentials.

### Prometheus + Grafana + Loki + Tempo

Four components wired together with Prometheus scraping and OTLP/`loki` pushes.

- Good, because each component is mature and widely adopted.
- Good, because Grafana dashboards and Prometheus alerting are industry standard.
- Bad, because logs, metrics, and traces live in three separate stores with three UIs to wire up.
- Bad, because four services (plus agents) is heavy for a dev-only observability setup.

### Elastic Stack (Elasticsearch, Kibana, Beats/APM)

Elasticsearch cluster with Kibana UI and Elastic APM for traces.

- Good, because Elasticsearch is proven at scale with rich querying.
- Bad, because the full stack is resource-hungry and license-encumbered for some features.
- Bad, because it does not speak OTLP natively; a collector gateway would still be required.
- Bad, because it is overkill for a demo project's dev environment.

### SigNoz

OpenTelemetry-native observability platform (ClickHouse-based).

- Good, because it is OTLP-native and covers traces, metrics, and logs.
- Bad, because it is primarily container-orchestration oriented; the compose setup is more complex
  (ClickHouse, query-service, frontend, collector).
- Bad, because more moving parts than OpenObserve for the same dev outcome.

## More Information

- NFR-09 — [docs/01-prd.md](../01-prd.md)
- Compose file: [infra/compose.obs-dev.yaml](../../infra/compose.obs-dev.yaml)
- Collector config: [infra/otel/otel-collector.yaml](../../infra/otel/otel-collector.yaml)
