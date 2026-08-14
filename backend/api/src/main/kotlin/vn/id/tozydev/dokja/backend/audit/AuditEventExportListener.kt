package vn.id.tozydev.dokja.backend.audit

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.logs.Logger
import io.opentelemetry.api.logs.Severity
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import tools.jackson.databind.json.JsonMapper

/**
 * Exports [AuditEvent]s directly through the OpenTelemetry Logs Bridge API. Active only where the
 * OpenTelemetry SDK is enabled.
 */
@Component
@Profile("prod-obs", "dev-obs", "test-obs")
class AuditEventExportListener(
    openTelemetry: OpenTelemetry,
    private val jsonMapper: JsonMapper,
) {

    private val logger: Logger =
        openTelemetry.logsBridge.loggerBuilder(AuditEventKeys.INSTRUMENTATION_SCOPE).build()

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    fun onAuditEvent(event: AuditEvent): Unit =
        with(logger.logRecordBuilder()) {
            setEventName(AuditEventKeys.EVENT_NAME)
            setSeverity(Severity.INFO)
            setTimestamp(event.timestamp)
            setBody(event.body)
            setAttribute(AuditEventKeys.ATTR_AUDIT_EVENT, true)
            setAttribute(AuditEventKeys.ATTR_ACTOR, event.actor)
            setAttribute(AuditEventKeys.ATTR_ACTION, event.action)
            setAttribute(AuditEventKeys.ATTR_RESOURCE_TYPE, event.resourceType)

            event.resourceId?.let { setAttribute(AuditEventKeys.ATTR_RESOURCE_ID, it.toString()) }
            event.sourceIp?.let { setAttribute(AuditEventKeys.ATTR_SOURCE_IP, it) }
            event.traceId?.let { setAttribute(AuditEventKeys.ATTR_TRACE_ID, it) }
            event.before?.let { setAttribute(AuditEventKeys.ATTR_BEFORE, json(it)) }
            event.after?.let { setAttribute(AuditEventKeys.ATTR_AFTER, json(it)) }

            emit()
        }

    private val AuditEvent.body: String
        get() = "audit: $action resource=${resourceType}/${resourceId}"

    private fun json(value: Any): String = jsonMapper.writeValueAsString(value)
}
