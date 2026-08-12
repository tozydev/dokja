package vn.id.tozydev.dokja.backend

import aws.sdk.kotlin.services.s3.S3Client
import io.opentelemetry.sdk.trace.export.SpanExporter
import java.time.Duration
import java.util.*
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.micrometer.metrics.test.autoconfigure.AutoConfigureMetrics
import org.springframework.boot.micrometer.tracing.opentelemetry.autoconfigure.otlp.OtlpTracingConnectionDetails
import org.springframework.boot.micrometer.tracing.test.autoconfigure.AutoConfigureTracing
import org.springframework.boot.opentelemetry.autoconfigure.logging.otlp.OtlpLoggingConnectionDetails
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.testcontainers.containers.GenericContainer
import vn.id.tozydev.dokja.backend.config.OtelTestcontainersConfiguration

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTracing(export = true)
@AutoConfigureMetrics(export = true)
@ActiveProfiles("integration-test", "test-obs")
@Import(OtelTestcontainersConfiguration::class)
@EnableAutoConfiguration(
    exclude =
        [
            DataSourceAutoConfiguration::class,
            HibernateJpaAutoConfiguration::class,
            FlywayAutoConfiguration::class,
            DataRedisAutoConfiguration::class,
        ]
)
class ObservabilityExportIntegrationTest {

    private val logger = LoggerFactory.getLogger(javaClass)

    @MockitoBean private lateinit var s3Client: S3Client

    @Autowired private lateinit var mockMvc: MockMvc

    @Autowired private lateinit var otelCollector: GenericContainer<*>

    @Autowired(required = false)
    private var otlpLoggingConnectionDetails: OtlpLoggingConnectionDetails? = null

    @Autowired(required = false)
    private var otlpTracingConnectionDetails: OtlpTracingConnectionDetails? = null

    @Autowired(required = false) private var spanExporter: SpanExporter? = null

    @Test
    fun `should wire otlp connection details from the collector container`() {
        assertNotNull(otlpLoggingConnectionDetails)
        assertNotNull(otlpTracingConnectionDetails)
        assertNotNull(spanExporter)
    }

    @Test
    fun `should export log records via OTLP`() {
        val marker = "observability-it-log-${UUID.randomUUID()}"
        logger.info("$marker hello dokja")

        await().atMost(Duration.ofSeconds(30)).untilAsserted {
            assertTrue(
                otelCollector.logs.contains(marker),
                "expected OTLP-exported log record with body '$marker', got:\n${otelCollector.logs}",
            )
        }
    }

    @Test
    fun `should export traces via OTLP`() {
        mockMvc.get("/api/v1/public/hello").andExpect { status { isOk() } }

        await().atMost(Duration.ofSeconds(30)).untilAsserted {
            assertTrue(
                otelCollector.logs.contains("http get /api/v1/public/hello"),
                "expected exported span 'http get /api/v1/public/hello', got:\n${otelCollector.logs}",
            )
        }
    }

    @Test
    fun `should export metrics via OTLP`() {
        mockMvc.get("/api/v1/public/hello").andExpect { status { isOk() } }

        await().atMost(Duration.ofSeconds(30)).untilAsserted {
            assertTrue(
                otelCollector.logs.contains("http.server.requests"),
                "expected exported metric http.server.requests, got:\n${otelCollector.logs}",
            )
        }
    }
}
