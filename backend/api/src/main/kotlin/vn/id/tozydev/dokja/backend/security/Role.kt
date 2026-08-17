package vn.id.tozydev.dokja.backend.security

/**
 * Application roles, mapped from Keycloak realm roles.
 *
 * The [realmRole] holds the exact realm-role name as configured in Keycloak; Spring Security
 * authorities are exposed as `ROLE_` + [name].
 */
enum class Role(val realmRole: String, val isAdmin: Boolean = false) {
    USER("user"),
    CONTENT_MANAGER("content-manager", isAdmin = true),
    MODERATOR("moderator", isAdmin = true),
    OPERATION_ADMIN("operation-admin", isAdmin = true),
    SYSTEM_ADMIN("system-admin", isAdmin = true);

    val authority = "ROLE_$name"

    /**
     * String constants mirroring [Role] names for use in Spring Security method annotations (e.g.
     * `@PreAuthorize("hasRole(...Role.Authorities.MODERATOR)")`).
     */
    object Authorities {
        const val USER = "USER"
        const val CONTENT_MANAGER = "CONTENT_MANAGER"
        const val MODERATOR = "MODERATOR"
        const val OPERATION_ADMIN = "OPERATION_ADMIN"
        const val SYSTEM_ADMIN = "SYSTEM_ADMIN"
    }

    companion object {
        private val byRealmRole = entries.associateBy { it.realmRole }

        val adminRoles = entries.filter { it.isAdmin }

        /** Resolves a Keycloak realm role name to the matching [Role], or null if unknown. */
        fun fromRealmRole(realmRole: String): Role? = byRealmRole[realmRole]
    }
}
