package vn.id.tozydev.dokja.backend.audit

import java.time.Instant

/** Structured audit event produced by audited actions. */
data class AuditEvent(
    val timestamp: Instant = Instant.now(),
    val actor: String,
    val action: String,
    val resourceType: String,
    val resourceId: String? = null,
    val before: Any? = null,
    val after: Any? = null,
    val sourceIp: String? = null,
    val traceId: String? = null,
)
