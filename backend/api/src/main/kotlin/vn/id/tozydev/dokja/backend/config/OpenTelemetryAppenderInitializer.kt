package vn.id.tozydev.dokja.backend.config

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("prod-obs", "dev-obs", "test-obs")
class OpenTelemetryAppenderInitializer(private val openTelemetry: OpenTelemetry) :
    InitializingBean {

    override fun afterPropertiesSet() = OpenTelemetryAppender.install(openTelemetry)
}
