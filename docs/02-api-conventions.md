# API CONVENTIONS: DOKJA PLATFORM

---

- **Project:** DOKJA Platform
- **Scope:** API Server (backend), consumed by Web App, Mobile App, and Admin Portal
- **Applies to:** `backend/api` (Spring Boot, Kotlin) and `frontend/packages/api-client` (generated
  TypeScript client)
- **Created:** 2026-08-11
- **Status:** Active

---

## 1. Overview

This document defines the conventions every HTTP API in DOKJA must follow. It is the single
reference for how URLs are named, how requests and responses are shaped, and how the API contract is
produced and consumed.

### 1.1 Design Principles

- **RESTful over HTTP/JSON.** Resources are named as nouns; state changes happen via HTTP methods.
- **Contract generated from backend code.** The OpenAPI 3.1 document is generated from the backend
  code (springdoc) and serves as the contract; the frontend generates a typed client from it (see
  [§11](#11-openapi-contract-tooling)).
- **Versioned.** Endpoints live under `/api/v{version}` (public) and `/admin` (internal) (see
  [§2.1](#21-api-zones)).
- **Raw data on success, structured errors.** Success responses return the resource representation
  directly; errors follow a standard format (see [§5](#5-response-format)).
- **Secure by default.** Every endpoint requires a valid Keycloak JWT (see
  [§7](#7-authentication--authorization)).

## 2. URLs

### 2.1 API Zones

The API is split into two zones, each with its own base path:

| Zone     | Base path        | Consumers           | Authentication                                          |
| -------- | ---------------- | ------------------- | ------------------------------------------------------- |
| Public   | `/api/{version}` | Web App, Mobile App | JWT bearer (see [§7](#7-authentication--authorization)) |
| Internal | `/admin`         | Admin Portal        | JWT bearer + admin role                                 |

- Public zone serves guest-facing and user-facing features (catalog, consumption, interaction,
  wallet, subscription, events, AI, notifications); internal zone serves management features
  (content, moderation, plans, events, accounts, dashboards) and is restricted to admin roles.
- Internal endpoints must never be exposed under the public base path.

### 2.2 Base Path & Versioning

Public endpoints are prefixed with `/api/v{version}`. The current version is `v1` (`/api/v1`).

- Versioning is **only** via the URL path. Never version via headers or media types.
- Breaking changes require a new major version (`/api/v2`). Additive, backward-compatible changes do
  not.
- The internal zone (`/admin`) is not versioned; it is deployed in lockstep with the Admin Portal.

### 2.3 Resource Naming

- Use **kebab-case** lowercase path segments: `/anime-information`, never `/animeInformation`,
  `/anime_information`, or `/AnimeInformation`.
- Use **plural nouns** for collections: `/animes`, `/comics`, `/novels`, `/episodes`, `/comments`.
- Use **singular nouns** for singleton resources only (e.g. `/user/me`, `/user/wallet`).
- Nest sub-resources under their parent when the sub-resource cannot exist without the parent:

```
/api/v1/animes/{animeId}/episodes
/api/v1/novels/{novelId}/chapters/{chapterId}/comments
```

- Resource identifiers in the path are named `{resourceId}` with camelCase, e.g. `{animeId}`,
  `{novelId}` (see [§8.1](#81-resource-identifiers)).
- **Actions** (non-CRUD operations) are expressed as a verb segment on the resource:

```
POST /api/v1/wallet/transactions/{transactionId}/retry
POST /api/v1/subscriptions/{subscriptionId}/activate
```

Prefer a dedicated noun/state-change endpoint over verbs where a natural resource exists (e.g.
`POST /api/v1/favorites` rather than `POST /api/v1/animes/{id}/favorite`).

### 2.4 Query Parameters

- Use **camelCase**: `?releasedAfter=2026-08-01`, never `?released_after` or `?released-after`.
- Pagination, filtering, and sorting have fixed names (see [§6](#6-pagination-filtering-sorting)).
- Booleans are `true`/`false`. Dates use ISO-8601 (`?releasedAfter=2026-08-01T00:00:00Z`).

## 3. HTTP Methods

| Method | Semantics                                                         | Idempotent | Examples                               |
| ------ | ----------------------------------------------------------------- | ---------- | -------------------------------------- |
| GET    | Read a resource or collection. Never changes state.               | Yes        | `GET /animes`, `GET /animes/{animeId}` |
| POST   | Create a resource, or trigger a non-idempotent action.            | No         | `POST /animes`, `POST /wallet/top-up`  |
| PUT    | Full replace of a resource. Body must contain the complete state. | Yes        | `PUT /animes/{animeId}`                |
| PATCH  | Partial update of a resource. Body contains only changed fields.  | No¹        | `PATCH /animes/{animeId}`              |
| DELETE | Delete a resource.                                                | Yes        | `DELETE /comments/{commentId}`         |

¹ PATCH is not guaranteed idempotent per RFC 5789; prefer PUT where idempotency is required.

- Use **PATCH**, not PUT, for partial updates such as anime lifecycle transitions that touch a
  single field (`PATCH /animes/{animeId} { "state": "paused" }`).
- GET and DELETE must never have a request body. Sending one results in `400 Bad Request`.

## 4. Status Codes

The API commits to a fixed, minimal set of status codes:

| Code | Name                  | When to use                                                                             |
| ---- | --------------------- | --------------------------------------------------------------------------------------- |
| 200  | OK                    | Successful GET, PUT, PATCH; successful POST that returns a representation               |
| 201  | Created               | POST that creates a resource; response contains `Location` header + full representation |
| 202  | Accepted              | Request accepted for async processing (background jobs, reconciliation)                 |
| 204  | No Content            | Successful DELETE, or POST with no representation to return                             |
| 400  | Bad Request           | Malformed body, invalid query, unknown enum value, validation failure                   |
| 401  | Unauthorized          | Missing or invalid JWT                                                                  |
| 403  | Forbidden             | Valid JWT but insufficient role/authority                                               |
| 404  | Not Found             | Resource does not exist (or is hidden — do not leak existence)                          |
| 409  | Conflict              | State conflict, e.g. BR-8.3 delete non-draft anime, BR-4.2 device consumption block     |
| 412  | Precondition Failed   | Version/etag mismatch on conditional update                                             |
| 422  | Unprocessable Entity  | Request well-formed but violates business rules (e.g. BR-1.3, BR-2.4)                   |
| 429  | Too Many Requests     | Rate limit exceeded (AI abuse control per F-AI-06, NFR-07)                              |
| 500  | Internal Server Error | Unexpected server error                                                                 |
| 503  | Service Unavailable   | Downstream dependency unavailable (Sepay, AI provider, storage)                         |

Rules:

- 404 vs 403: archived/hidden content (BR-8.5) and age-blocked content (18+) return **404** so
  unauthorized access is indistinguishable from absence.
- 400 vs 422: malformed syntax / validation constraints → 400; well-formed but violates business
  logic → 422.
- 3xx redirects are never used by the API.
- Errors are formatted per [§5.3](#53-error-responses).

## 5. Response Format

All responses are JSON. Field names use **camelCase**. Success responses return the resource
representation directly — no wrapping envelope. Error responses follow RFC 9457
([Problem Details](https://www.rfc-editor.org/rfc/rfc9457.html)) with platform extensions.

### 5.1 Success Responses

- `GET` returns the resource object or collection array directly.
- `POST`, `PUT`, `PATCH` return the affected resource representation. `201 Created` responses
  additionally return a `Location` header pointing to the created resource.
- `DELETE` returns `204 No Content`.

Example — create an anime:

```json
{
  "id": "0197b9b6-7c4f-7f2e-8c1a-9b2c3d4e5f60",
  "name": "One Piece",
  "state": "draft",
  "ageRating": "p"
}
```

### 5.2 Empty Responses

- `DELETE`, or a POST that creates nothing, returns `204 No Content` with an empty body.
- A query that matches nothing returns `200` with `[]` (collection) or a page with zero items —
  never `404`.

### 5.3 Error Responses

All errors follow RFC 9457, served with `Content-Type: application/problem+json` and the appropriate
status code:

```json
{
  "type": "https://api.dokja.example/problems/anime-not-found",
  "title": "Anime not found",
  "status": 404,
  "detail": "Anime not found",
  "instance": "/api/v1/animes/0197b9b6-7c4f-7f2e-8c1a-9b2c3d4e5f60",
  "requestId": "0197b9c0-1111-2222-3333-444455556666",
  "timestamp": "2026-08-11T09:30:00Z",
  "code": "ANIME_NOT_FOUND",
  "errors": [
    {
      "field": "name",
      "code": "FIELD_REQUIRED",
      "message": "name is required"
    }
  ]
}
```

| Member      | Required | Description                                                                                    |
| ----------- | -------- | ---------------------------------------------------------------------------------------------- |
| `type`      | yes      | URI identifying the problem type (may be a generic `about:blank`)                              |
| `title`     | yes      | Short, human-readable summary of the problem                                                   |
| `status`    | yes      | HTTP status code                                                                               |
| `detail`    | no       | Detailed, human-readable explanation (Vietnamese — the platform display language per PRD §4.3) |
| `instance`  | no       | URI of the specific request occurrence (the request path)                                      |
| `requestId` | yes      | Correlation ID of the failed request (see [§10](#10-observability))                            |
| `timestamp` | yes      | ISO-8601 UTC timestamp of the failure                                                          |
| `code`      | yes      | Extension member: stable, machine-readable error code in `SCREAMING_SNAKE_CASE`                |
| `errors`    | no       | Extension member: field-level validation errors; one entry per field                           |

Error codes follow the pattern `{DOMAIN}_{REASON}`, e.g. `WALLET_INSUFFICIENT_BALANCE`,
`ANIME_STATE_CONFLICT`, `AGE_RESTRICTED`. `errors[].code` values are `FIELD_REQUIRED`,
`FIELD_INVALID`, `FIELD_TOO_LONG`, `FIELD_OUT_OF_RANGE`.

## 6. Pagination, Filtering, Sorting

### 6.1 Pagination

Collection endpoints return pages. Two strategies are allowed; pick per endpoint and document it in
the OpenAPI spec:

**Offset pagination** (default — admin lists, management screens):

```
GET /api/v1/animes?page=1&size=20
```

```json
{
  "items": [{ "id": "...", "name": "One Piece" }],
  "page": 1,
  "size": 20,
  "totalItems": 143,
  "totalPages": 8
}
```

- `page` is 1-based. `size` default 20, max 100 (larger → `400`).
- Sort stability: every pageable query must include a deterministic tie-breaker (e.g. `id`).

**Cursor pagination** (default for feeds/history — comments, notifications, read history, where the
collection grows monotonically):

```
GET /api/v1/comments?animeId=...&limit=20&cursor=0197b9b6-...
```

```json
{
  "items": [{ "id": "...", "content": "..." }],
  "nextCursor": "0197b9c0-...",
  "hasMore": true
}
```

- `cursor` is an opaque token (base64 of the last item's sort key); clients never decode it.
- `limit` default 20, max 100.

### 6.2 Filtering

- Filters are plain query parameters named after the field, camelCase:
  `?state=ongoing&genre=action`.
- Multiple values for one field mean **IN**: `?genre=action&genre=adventure`.
- Range and substring filters use an operator prefix on the right-hand side, separated by a colon:

| Operator      | Meaning         | Example                     |
| ------------- | --------------- | --------------------------- |
| (none)        | equality / IN   | `?state=ongoing`            |
| `gt` / `lt`   | greater / less  | `?releasedAt=gt:2026-08-01` |
| `gte` / `lte` | greater / equal | `?price=gte:1000`           |
| `contains`    | substring       | `?name=contains:one`        |

- Unknown filter parameters → `400`.
- Complex domain filters use dedicated query params documented per endpoint.

### 6.3 Sorting

- Use fixed `sort` parameter: `?sort=releasedAt:desc,name:asc`.
- Sortable fields must be enumerated in the OpenAPI spec; unknown fields → `400`.

## 7. Authentication & Authorization

### 7.1 Authentication

- All endpoints require `Authorization: Bearer <JWT>` issued by Keycloak (ADR-0001).
- The JWT `sub` is the user id; roles arrive as realm roles mapped to Spring Security authorities.
- Tokens are short-lived; refresh happens via the Keycloak OIDC flow, never inside the API.

### 7.2 Authorization

- Authorization is enforced server-side — never by the frontend alone.
- Role-to-zone mapping:

| Role            | Zone     |
| --------------- | -------- |
| User            | Public   |
| Content Manager | Internal |
| Moderator       | Internal |
| Operation Admin | Internal |
| System Admin    | Internal |

- Age-restricted content (P/13+/16+/18+): serve only if the JWT `birthdate` claim satisfies the
  label; otherwise return `404` (see [§4](#4-status-codes)).

## 8. IDs & Formats

### 8.1 Resource Identifiers

- All resource IDs are **UUIDv7** (`0197b9b6-7c4f-7f2e-8c1a-9b2c3d4e5f60`) — time-sortable and
  index-friendly for cursor pagination.
- Path variables are UUIDs: `GET /animes/{animeId}` where `animeId` is a UUID. Non-UUID → `400`.

### 8.2 Date & Time

- All timestamps are ISO-8601 with UTC and `Z` suffix: `"2026-08-11T09:30:00Z"`.
- Never local time, never offsets like `+07:00` in API payloads.
- Date-only values (e.g. date of birth) use `"yyyy-MM-dd"`.

### 8.3 Money & Coin

- All amounts are **VND**, expressed as an integer in **minor units** (no decimals): `price: 19000`.
- Coin balances are integers: `"paidCoin": 500, "bonusCoin": 120`.
- Never use floating point for money.

### 8.4 Enums

- Enum values in JSON use **lower snake_case**: `"state": "ongoing"`,
  `"ageRating": "p" | "r_13" | "r_16" | "r_18"`.
- Unknown enum values → `400` with an `errors` entry for the offending field.

### 8.5 Nulls & Optionality

- Omit optional fields instead of sending `null` where the field is not applicable.
- Never return `null` for arrays — return `[]`.

## 9. Idempotency

- State-changing operations with financial consequences — coin top-up, subscription activation,
  chapter purchase (BR-6, F-WALLET-02/07) — must accept an `Idempotency-Key` header:

```
POST /api/v1/wallet/top-up
Idempotency-Key: 0197b9b6-...
```

- The key is a client-generated UUIDv7; the server stores it with the first request's response.
- A repeated request with the same key returns the original response (and status) without
  re-executing; keys expire after 24 hours.
- Other POSTs may support it; the OpenAPI spec marks which operations require it.

## 10. Observability

- Every request receives a `X-Request-ID` (UUIDv7), generated by the server if the client did not
  send one; it is echoed on the response and included in error responses (NFR-09).
- The request id propagates into logs, metrics, and distributed traces.
- All responses include `X-Request-ID`; all logs include the request id and authenticated user id.

## 11. OpenAPI Contract Tooling

The OpenAPI 3.1 document is the single source of truth for the contract.

### 11.1 Backend (Producer)

- The backend exposes the spec at `GET /v3/api-docs` (springdoc-openapi).
- Every controller is annotated with `@Tag`, `@Operation`, `@Parameter`, `@ApiResponse`, and schema
  annotations so the generated spec is complete and human-readable.
- DTOs use camelCase fields — name fields to match.

### 11.2 Frontend (Consumer)

- `frontend/packages/api-client` is **generated** from the backend spec; handwritten API clients are
  forbidden.
- Web and Admin apps depend on `@dokja/api-client`; they never hand-roll fetch wrappers.
- Generation runs as part of the frontend build; a mismatch between spec and code fails the build.

### 11.3 Validation

- Request DTOs are validated with `jakarta.validation` annotations (`@NotBlank`, `@Size`, `@Min`,
  ...); violations produce `400` with `errors` entries in the response body.
- The OpenAPI spec is the contract for frontend-backend integration; documentation drift is a build
  failure, not a review comment.

## 13. References

- [Product Requirements Document (01-prd.md)](01-prd.md)
- [ADR-0001 — Use Keycloak for authentication and authorization](decisions/0001-use-keycloak-for-authentication-and-authorization.md)
- [ADR-0002 — Adopt OpenAPI contract-first API conventions](decisions/0002-adopt-openapi-contract-first-api-conventions.md)
- Requirement codes used throughout (`BR-*`, `F-*`, `NFR-*`) are defined in the PRD.
