package vn.id.tozydev.dokja.backend.audit

import aws.sdk.kotlin.services.s3.S3Client
import java.time.Duration
import java.util.*
import javax.sql.DataSource
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.jdbc.support.JdbcTransactionManager
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.testcontainers.containers.GenericContainer
import vn.id.tozydev.dokja.backend.config.OtelTestcontainersConfiguration
import vn.id.tozydev.dokja.backend.config.PostgresTestcontainersConfiguration

/**
 * Exercises the transactional delivery contract of [AuditEventExportListener]: events published
 * inside an active transaction must be exported only after commit and must be suppressed entirely
 * when the transaction rolls back.
 */
@SpringBootTest
@ActiveProfiles("integration-test", "test-obs")
@Import(
    OtelTestcontainersConfiguration::class,
    PostgresTestcontainersConfiguration::class,
    TransactionalAuditTestConfiguration::class,
)
@EnableAutoConfiguration(
    exclude =
        [
            HibernateJpaAutoConfiguration::class,
            DataJpaRepositoriesAutoConfiguration::class,
            FlywayAutoConfiguration::class,
            DataRedisAutoConfiguration::class,
        ]
)
class AuditEventTransactionalExportIntegrationTest {

    @MockitoBean private lateinit var s3Client: S3Client

    @Autowired private lateinit var auditEventPublisher: AuditEventPublisher

    @Autowired private lateinit var transactionManager: PlatformTransactionManager

    @Autowired private lateinit var otelCollector: GenericContainer<*>

    @Test
    fun `exports audit event only after commit`() {
        val resourceId = UUID.randomUUID().toString()

        TransactionTemplate(transactionManager).execute {
            auditEventPublisher.publish(
                action = "test_audit_action_committed",
                resourceType = "test-resource",
                resourceId = resourceId,
                after = mapOf("note" to "committed"),
            )
            assertFalse(
                otelCollector.logs.contains(
                    "audit: test_audit_action_committed resource=test-resource/$resourceId"
                ),
                "event must not be exported before commit",
            )
        }

        await().atMost(Duration.ofSeconds(30)).untilAsserted {
            assertTrue(
                otelCollector.logs.contains(
                    "audit: test_audit_action_committed resource=test-resource/$resourceId"
                ),
                "expected committed event in collector, got:\n${otelCollector.logs}",
            )
        }
    }

    @Test
    fun `does not export audit event when transaction rolls back`() {
        val rolledBackId = UUID.randomUUID().toString()
        val committedId = UUID.randomUUID().toString()

        TransactionTemplate(transactionManager).execute { status ->
            status.setRollbackOnly()
            auditEventPublisher.publish(
                action = "test_audit_action_rolled_back",
                resourceType = "test-resource",
                resourceId = rolledBackId,
                after = mapOf("note" to "rolled back"),
            )
        }

        TransactionTemplate(transactionManager).execute {
            auditEventPublisher.publish(
                action = "test_audit_action_committed",
                resourceType = "test-resource",
                resourceId = committedId,
                after = mapOf("note" to "committed"),
            )
        }

        await().atMost(Duration.ofSeconds(30)).untilAsserted {
            assertTrue(
                otelCollector.logs.contains(
                    "audit: test_audit_action_committed resource=test-resource/$committedId"
                ),
                "expected committed event in collector, got:\n${otelCollector.logs}",
            )
        }

        await().pollDelay(Duration.ofSeconds(1)).atMost(Duration.ofSeconds(5)).untilAsserted {
            assertFalse(
                otelCollector.logs.contains(
                    "audit: test_audit_action_rolled_back resource=test-resource/$rolledBackId"
                ),
                "rolled-back event must not be exported",
            )
        }
    }
}

@TestConfiguration(proxyBeanMethods = false)
class TransactionalAuditTestConfiguration {

    @Bean
    fun transactionManager(dataSource: DataSource): PlatformTransactionManager =
        JdbcTransactionManager(dataSource)
}
