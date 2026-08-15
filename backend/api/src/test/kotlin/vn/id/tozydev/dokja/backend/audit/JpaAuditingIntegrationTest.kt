package vn.id.tozydev.dokja.backend.audit

import jakarta.persistence.EntityManager
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import vn.id.tozydev.dokja.backend.config.PostgresTestcontainersConfiguration
import vn.id.tozydev.dokja.testfixtures.AuditableTestEntity
import vn.id.tozydev.dokja.testfixtures.AuditableTestEntityRepository

@DataJpaTest(
    excludeAutoConfiguration = [FlywayAutoConfiguration::class],
    properties = ["spring.jpa.hibernate.ddl-auto=create-drop"],
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(JpaAuditingAutoConfiguration::class)
@Import(PostgresTestcontainersConfiguration::class, JwtAuditorAware::class)
@EntityScan(basePackageClasses = [AuditableTestEntity::class])
@EnableJpaRepositories(basePackageClasses = [AuditableTestEntityRepository::class])
class JpaAuditingIntegrationTest {

    @Autowired private lateinit var repository: AuditableTestEntityRepository

    @Autowired private lateinit var entityManager: EntityManager

    @AfterEach
    fun cleanUp() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `populates created metadata with authenticated principal`() {
        authenticate("alice")

        val saved = repository.save(AuditableTestEntity(name = "A"))

        assertNotNull(saved.createdAt)
        assertEquals("alice", saved.createdBy)
        assertNotNull(saved.updatedAt)
        assertEquals("alice", saved.updatedBy)
    }

    @Test
    fun `populates created metadata with system actor when unauthenticated`() {
        val saved = repository.save(AuditableTestEntity(name = "B"))

        assertNotNull(saved.createdAt)
        assertEquals(SYSTEM_ACTOR, saved.createdBy)
        assertEquals(SYSTEM_ACTOR, saved.updatedBy)
    }

    @Test
    fun `updates modified metadata while preserving created metadata`() {
        authenticate("alice")
        val created = repository.saveAndFlush(AuditableTestEntity(name = "C"))

        entityManager.clear()
        val inserted = repository.findById(created.id!!).orElseThrow()
        val createdAt = inserted.createdAt
        val createdBy = inserted.createdBy

        authenticate("bob")
        inserted.name = "C2"
        repository.saveAndFlush(inserted)

        entityManager.clear()
        val updated = repository.findById(created.id!!).orElseThrow()

        assertEquals(createdAt, updated.createdAt)
        assertEquals(createdBy, updated.createdBy)
        assertEquals("bob", updated.updatedBy)
        assertTrue(updated.updatedAt!!.isAfter(createdAt!!))
    }

    private fun authenticate(subject: String) {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(subject, null, emptyList())
    }
}
