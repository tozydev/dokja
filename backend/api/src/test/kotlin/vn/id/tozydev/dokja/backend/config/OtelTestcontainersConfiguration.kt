package vn.id.tozydev.dokja.backend.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import org.testcontainers.utility.MountableFile

@TestConfiguration(proxyBeanMethods = false)
class OtelTestcontainersConfiguration {

    @Bean
    @ServiceConnection(name = "otel/opentelemetry-collector-contrib")
    fun otelCollector(): GenericContainer<*> =
        GenericContainer(otelCollectorImage)
            .withExposedPorts(OTLP_GRPC_PORT, OTLP_HTTP_PORT)
            .withCopyFileToContainer(
                MountableFile.forClasspathResource("otel/otel-collector.yaml"),
                "/etc/otelcol/config.yaml",
            )

    companion object {
        private val otelCollectorImage =
            DockerImageName.parse("otel/opentelemetry-collector-contrib:0.158.0")
        private const val OTLP_GRPC_PORT = 4317
        private const val OTLP_HTTP_PORT = 4318
    }
}
