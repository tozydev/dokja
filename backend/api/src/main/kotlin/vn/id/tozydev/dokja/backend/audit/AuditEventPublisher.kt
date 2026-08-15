package vn.id.tozydev.dokja.backend.audit

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import vn.id.tozydev.dokja.backend.error.TraceIdResolver

/**
 * Publishes [AuditEvent]s through Spring's application event bus. The event is enriched with the
 * current actor, source IP, and OpenTelemetry trace ID so the correlation data is captured at the
 * moment the action happens.
 */
@Component
class AuditEventPublisher(
    private val applicationEvents: ApplicationEventPublisher,
    private val traceIdResolver: TraceIdResolver,
) {

    fun publish(
        action: String,
        resourceType: String,
        resourceId: String? = null,
        before: Any? = null,
        after: Any? = null,
    ) {
        applicationEvents.publishEvent(
            AuditEvent(
                actor = currentActor(),
                action = action,
                resourceType = resourceType,
                resourceId = resourceId,
                before = before,
                after = after,
                sourceIp = currentSourceIp(),
                traceId = traceIdResolver.currentTraceId(),
            )
        )
    }

    private fun currentSourceIp(): String? {
        val request =
            (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request
        return request?.remoteAddr
    }
}
