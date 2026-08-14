package vn.id.tozydev.dokja.backend.audit

import aws.sdk.kotlin.services.s3.S3Client
import java.time.Duration
import java.util.*
import kotlin.test.assertTrue
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.testcontainers.containers.GenericContainer
import vn.id.tozydev.dokja.backend.config.OtelTestcontainersConfiguration

@SpringBootTest
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
class AuditEventExportIntegrationTest {

    @MockitoBean private lateinit var s3Client: S3Client

    @Autowired private lateinit var auditEventPublisher: AuditEventPublisher

    @Autowired private lateinit var otelCollector: GenericContainer<*>

    @Test
    fun `exports audit events as otlp log records`() {
        val resourceId = UUID.randomUUID()
        try {
            SecurityContextHolder.getContext().authentication =
                UsernamePasswordAuthenticationToken("user-123", null, emptyList())
            auditEventPublisher.publish(
                action = "test_audit_action",
                resourceType = "test-resource",
                resourceId = resourceId,
                after = mapOf("note" to "audit e2e"),
            )
        } finally {
            SecurityContextHolder.clearContext()
        }

        await().atMost(Duration.ofSeconds(30)).untilAsserted {
            assertTrue(
                otelCollector.logs.contains(
                    "audit: test_audit_action resource=test-resource/$resourceId"
                ),
                "expected OTLP-exported audit event, got:\n${otelCollector.logs}",
            )
            assertTrue(
                otelCollector.logs.contains("audit.actor"),
                "expected audit actor attribute, got:\n${otelCollector.logs}",
            )
        }
    }
}
