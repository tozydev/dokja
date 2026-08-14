# Backend Error Handling Guide

How to produce error responses in `backend`. The wire format, status codes, and member contract are
defined in [API Conventions §4 & §5.3 (02-api-conventions.md)](../02-api-conventions.md) — this
guide covers only usage and best practices.

## 1. Throw a Domain Error

Domain (business-rule) errors are modeled as exceptions. Two pieces are needed: a **code** (a marker
implementing `DomainErrorCode`) and an **exception** extending `DomainException`.

```kotlin
data object WalletInsufficientBalance : DomainErrorCode

class WalletInsufficientBalanceException :
    DomainException(
        errorCode = WalletInsufficientBalance,
        status = HttpStatus.CONFLICT,
        title = "Insufficient balance",
        detail = "Your balance is not enough",
    )

```

Throw it from the service/domain layer — never from a controller:

```kotlin
fun transfer(from: UUID, to: UUID, amount: Long) {
    if (wallet(from).balance < amount) throw WalletInsufficientBalanceException()
}
```

The response is produced automatically (RFC 9457 body with the `code` extension, `X-Trace-Id`
header, matching HTTP status). The serialized code — `wallet_insufficient_balance` — is derived from
the marker's class name in lower snake_case.

## 2. Best Practices

**Naming and stability**

- Name codes `{Domain}{Reason}` in PascalCase, e.g. `AnimeStateConflict`, `AgeRestricted`. The API
  exposes them lower snake_case (`anime_state_conflict`).
- Codes are part of the public contract: once published, never rename or delete one without a major
  version bump.
- Code is the client's stable key for branching; make the _title_ human-friendly, not the code.

**Status selection**

- Use the status table in [Conventions §4](../02-api-conventions.md#4-status-codes). Common pairs:
  - 409 for state conflicts (e.g., deleting non-draft anime)
  - 422 for well-formed requests that violate business rules
  - 404 for hidden/age-blocked content — never 403 (do not leak existence)
- `DomainException` is for **business rules only**. Never throw it for input validation, malformed
  requests, or framework failures.

**Detail content**

- `detail` is shown to users and keep it human-readable.
- Never leak internals: no SQL, no stack traces, no exception class names, no raw error messages
  from dependencies (HTTP clients, S3, Keycloak).
- If an error must not reveal the reason (e.g. "wrong credentials"), return a generic message and
  log the real cause server-side.

**Exception hygiene**

- Throw the most specific `DomainException` subtype; don't wrap everything in a generic
  `DomainException("something failed")`.
- Don't catch an exception only to rethrow a less specific one — preserve the original.
- Prefer `data object` markers (singleton) over classes unless the code carries data.

## 3. Validation Errors

Request validation is automatic — no manual code needed:

- Annotate DTOs with `jakarta.validation` constraints (`@NotBlank`, `@Size`, `@Min`, ...).
- The framework returns `400` with `errors[]` entries (`field`/`parameter` + `message`) and no
  `code`.
