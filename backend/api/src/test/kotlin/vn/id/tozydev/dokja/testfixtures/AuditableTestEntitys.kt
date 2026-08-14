package vn.id.tozydev.dokja.testfixtures

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import vn.id.tozydev.dokja.backend.audit.AbstractAuditableEntity

@Entity
class AuditableTestEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    var name: String,
) : AbstractAuditableEntity()

@Repository interface AuditableTestEntityRepository : JpaRepository<AuditableTestEntity, Long>
