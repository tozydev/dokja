---
number: 1
title: Use Keycloak for authentication and authorization
status: proposed
date: 2026-08-10
---

# Use Keycloak for authentication and authorization

## Context and Problem Statement

The platform must establish user identity (authentication) and control access to API resources
(authorization) across the web frontend, admin frontend, and backend API. How should identity and
access control be implemented?

## Decision Drivers

- Model standard industry security patterns (OIDC/JWT) rather than bespoke security code.
- Self-hosted dev via docker-compose; no external SaaS dependency or account.
- Both frontends need login; the backend needs role-based protection (e.g. ADMIN vs USER).
- Password hashing, token lifecycle, and credential storage are risky to implement in-house and out
  of scope.

## Considered Options

- Keycloak as external IdP
- Custom authentication (own user store + self-issued JWT)
- Managed SaaS IdP (Auth0 / Cognito)

## Decision Outcome

Chosen option: "Keycloak as external IdP", because it is the only option that satisfies all drivers:
self-hosted, open-source, OIDC-compliant, offloading credential storage and token lifecycle, with
realm roles mapping onto Spring Security authorities. Custom auth maximizes security risk for no
product benefit; a managed SaaS IdP breaks the self-hosted driver.

The platform adopts Keycloak end-to-end: frontends authenticate via OIDC authorization code flow
with PKCE and obtain JWT access tokens; the backend resource server validates tokens against
Keycloak and derives authorities from realm roles.

### Consequences

- Good, because the backend owns no user store, password hashing, or token lifecycle.
- Good, because realm roles map directly to Spring Security authorities, managed centrally in the
  Keycloak admin console.
- Good, because web and admin frontends share one IdP, giving SSO.
- Bad, because Keycloak is a hard operational dependency and an additional service to run,
  configure, and upgrade.
- Bad, because the frontends must handle token expiry and refresh flows.
