---
number: 2
title: Adopt OpenAPI contract-first API conventions
status: accepted
date: 2026-08-11
---

# Adopt OpenAPI contract-first API conventions

## Context and Problem Statement

We need a method to reduce API maintenance effort.

## Decision Drivers

- Single source of truth for API contracts between backend and frontend.
- No handwritten API specs or clients.
- Follow non-functional requirements in PRD.

## Considered Options

- Generated OpenAPI 3.1 spec with generated TypeScript client
- Handwritten typed client plus prose documentation
- Kotlin Multiplatform shared DTOs & contracts

## Decision Outcome

Chosen option: "Generated OpenAPI 3.1 with generated TypeScript client", because it is the only
option that gives every consumer a single, machine-readable contract without forcing the frontend
off its TypeScript/React stack. The backend produces the OpenAPI 3.1 document and implements it;
frontend consumes from the generated client with a little effort to keep sync.

### Consequences

- Good, because the contract is machine-readable; schema drift between backend and frontend becomes
  a build failure instead of a runtime bug.
- Good, because web and admin apps get a typed client generated from the same source of truth.
- Good, because the conventions doc gives one consistent API surface across all consumers (Web,
  Mobile, Admin).
- Bad, because code generation adds a step to the frontend build and couples it to the backend spec.
-

## Pros and Cons of the Options

### Handwritten typed client plus prose documentation

Frontend hand-writes a typed API client following a prose conventions doc; no shared machine
artifact.

- Good, because no additional tooling or dependency is required.
- Bad, because nothing enforces the contract; payload drift is discovered at runtime.
- Bad, because every consumer must duplicate the contract in its own types.
- Bad, because documentation and code can diverge without detection.

### Kotlin Multiplatform shared DTOs & contracts

Define DTOs and contracts once in Kotlin Multiplatform and share them with the frontend via
Kotlin/JS.

- Good, because the contract is expressed in code in one language.
- Bad, because Kotlin/JS `@JsExport` limits the export surface and restricts what can be shared
  cleanly with the TypeScript ecosystem.
- Bad, because it introduces a second language toolchain into the frontend build.
- Bad, because ecosystem/typing interop with TypeScript libraries remains friction-prone.

## More Information

- Conventions detail: [docs/02-api-conventions.md](../../docs/02-api-conventions.md)
- Relates to
  [ADR-0001: Use Keycloak for authentication and authorization](0001-use-keycloak-for-authentication-and-authorization.md)
  (authentication and role mapping for the API)
