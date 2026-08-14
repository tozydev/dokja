package vn.id.tozydev.dokja.backend.error

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.Span
import java.security.SecureRandom
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

/** Resolves the correlation id used for the `X-Trace-Id` header of error responses. */
interface TraceIdResolver {
    fun currentTraceId(): String
}

@AutoConfiguration
class TraceIdResolverAutoConfiguration {

    @Bean
    @ConditionalOnBean(OpenTelemetry::class)
    fun openTelemetryTraceIdResolver(): TraceIdResolver =
        object : TraceIdResolver {
            override fun currentTraceId() = Span.current().spanContext.traceId
        }

    @Bean
    @ConditionalOnMissingBean(OpenTelemetry::class)
    fun randomTraceIdResolver(): TraceIdResolver =
        object : TraceIdResolver {
            override fun currentTraceId() = randomHex()
        }
}

internal fun randomHex(): String {
    val bytes = ByteArray(16)
    SecureRandom().nextBytes(bytes)
    return bytes.toHexString()
}
