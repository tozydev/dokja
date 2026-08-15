package vn.id.tozydev.dokja.backend.audit

/** OpenTelemetry attribute names used during OTLP export of [AuditEvent]s. */
object AuditEventKeys {
    const val EVENT_NAME = "dokja.audit"
    const val INSTRUMENTATION_SCOPE = "dokja.audit"

    const val ATTR_AUDIT_EVENT = "audit.event"
    const val ATTR_ACTOR = "audit.actor"
    const val ATTR_ACTION = "audit.action"
    const val ATTR_RESOURCE_TYPE = "audit.resource.type"
    const val ATTR_RESOURCE_ID = "audit.resource.id"
    const val ATTR_SOURCE_IP = "audit.source.ip"
    const val ATTR_TRACE_ID = "audit.trace.id"
    const val ATTR_BEFORE = "audit.before"
    const val ATTR_AFTER = "audit.after"
}
