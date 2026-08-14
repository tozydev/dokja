---
number: 4
title: Adopt a dual-track audit framework with OTLP-exported audit events and JPA row metadata
status: proposed
date: 2026-08-14
links:
  - target: 3
    kind: relatesto
---

# Adopt a dual-track audit framework with OTLP-exported audit events and JPA row metadata

## Context and Problem Statement

NFR-10 requires that all important actions (login/logout, content edits, configuration changes,
violation handling, payment transactions) be recorded with actor, timestamp, and before/after
values, and that logs are tamper-evident and retained per compliance. In addition, the team wants
row-level provenance so every persisted row carries who created and last modified it.

## Decision Drivers

- NFR-10 — audit records must capture actor, timestamp, and before/after values.
- Reuse the existing OpenTelemetry Collector → OpenObserve pipeline (ADR-3) instead of adding new
  infrastructure; keep ops overhead low for a learning/demo project.
- NFR-11 loose coupling — modules communicate through well-defined interfaces, so audit event
  production and consumption should stay decoupled.
- Row-level provenance — `createdBy`/`updatedBy` and timestamps on every entity for quick reference.

## Considered Options

- Dual-track audit: OTLP-exported audit events + JPA row metadata
- Postgres audit table (INSERT-only `audit_trail`)
- Dedicated audit store (separate OpenObserve stream / external SIEM)

## Decision Outcome

Chosen option: "Dual-track audit: OTLP-exported audit events + JPA row metadata", because it
satisfies NFR-10's event-style audit requirements with zero new infrastructure while also giving
row-level provenance through JPA auditing, all through the OTLP pipeline already adopted in ADR-3.

The framework combines two complementary mechanisms:

- **Event-based audit**: audited actions publish a dedicated audit event (actor, action, resource
  type/id, before/after, timestamp, trace ID, source IP) through Spring's application event bus. A
  listener exports it directly through the OpenTelemetry Logs Bridge API as a structured OTLP log
  record → Collector → OpenObserve, setting the fields as typed OTel attributes and an `audit.event`
  discriminator rather than routing through logback.
- **Row-based metadata audit**: a base `@MappedSuperclass` entity with `createdAt`/`createdBy`/
  `updatedAt`/`updatedBy`, backed by `@EnableJpaAuditing` and an `AuditorAware` that resolves the
  Keycloak JWT subject (from ADR-1) as the actor.
- **Before/after representation**: insert and delete produce full snapshots of the resource state;
  update and patch produce a field-level diff (field, before, after).

### Consequences

- Good, because no new infrastructure is required; the audit stream reuses the OpenTelemetry
  collector and OpenObserve deployment.
- Good, because event publication decouples audit producers from consumers.
- Good, because row-level provenance comes from JPA auditing with minimal per-entity code.
- Bad, because OpenObserve logs are mutable and not tamper-evident; this is an accepted limitation
  for the learning/demo scope. Hardening (append-only storage, hash-chaining) is deferred to a
  future decision if compliance-grade tamper-evidence is ever required.
- Bad, because before/after values for event-based audit must be captured explicitly by domain code;
  there is no automatic Envers-style diff.
- Neutral, because OTLP log export is not transactional with the database change; events are emitted
  after commit, so a failed export does not roll back the audited action.

## More Information

- NFR-10 — [docs/01-prd.md](../01-prd.md)
- ADR-3 —
  [0003-adopt-opentelemetry-collector-and-openobserve-for-observability.md](0003-adopt-opentelemetry-collector-and-openobserve-for-observability.md)
- ADR-1 (actor identity) —
  [0001-use-keycloak-for-authentication-and-authorization.md](0001-use-keycloak-for-authentication-and-authorization.md)
