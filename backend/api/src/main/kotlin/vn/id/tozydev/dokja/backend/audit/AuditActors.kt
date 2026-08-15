package vn.id.tozydev.dokja.backend.audit

import org.springframework.security.core.context.SecurityContextHolder

/** Fallback actor used when no authenticated principal is available (background jobs, etc.). */
const val SYSTEM_ACTOR = "system"

/**
 * Resolves the actor of the current operation: the authenticated JWT subject when available,
 * otherwise [SYSTEM_ACTOR]. Used both by JPA auditing and audit event publishing.
 */
internal fun currentActor(): String =
    SecurityContextHolder.getContext().authentication?.name.orEmpty().ifBlank { SYSTEM_ACTOR }
