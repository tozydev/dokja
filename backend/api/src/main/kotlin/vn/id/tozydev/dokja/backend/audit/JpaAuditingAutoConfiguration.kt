package vn.id.tozydev.dokja.backend.audit

import jakarta.persistence.EntityManagerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

/**
 * Enables Spring Data JPA auditing: `@CreatedDate`/`@LastModifiedDate`/`@CreatedBy`/
 * `@LastModifiedBy` fields are populated automatically. The auditor is resolved by
 * [JwtAuditorAware].
 *
 * Use autoconfiguration because some tests do not have database operations.
 */
@AutoConfiguration(after = [HibernateJpaAutoConfiguration::class])
@ConditionalOnBean(EntityManagerFactory::class)
@EnableJpaAuditing
class JpaAuditingAutoConfiguration
