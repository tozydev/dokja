package vn.id.tozydev.dokja.backend.audit

import java.util.*
import org.springframework.data.domain.AuditorAware
import org.springframework.stereotype.Component

/**
 * Supplies the actor for JPA auditing: the Keycloak JWT subject of the authenticated principal,
 * falling back to [SYSTEM_ACTOR] for unauthenticated or background operations.
 */
@Component
class JwtAuditorAware : AuditorAware<String> {

    override fun getCurrentAuditor(): Optional<String> = Optional.of(currentActor())
}
