package vn.id.tozydev.dokja.backend.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

/**
 * Converts a Keycloak JWT into a Spring Security authentication token, mapping the Keycloak
 * `realm_access.roles` claim to `ROLE_`-prefixed granted authorities (e.g. `ROLE_MODERATOR`).
 *
 * Only roles recognized by [Role] are mapped; the principal is always authenticated regardless of
 * whether it carries any known role.
 */
class KcRealmRoleJwtAuthenticationConverter : Converter<Jwt, AbstractAuthenticationToken> {

    override fun convert(jwt: Jwt): AbstractAuthenticationToken =
        JwtAuthenticationToken(jwt, jwt.authorities())
}

private fun Jwt.authorities(): MutableCollection<GrantedAuthority> =
    realmRoles()
        .mapNotNull { Role.fromRealmRole(it) }
        .map { SimpleGrantedAuthority("ROLE_${it.name}") as GrantedAuthority }
        .toMutableList()

private const val REALM_ACCESS_CLAIM = "realm_access"
private const val ROLES_CLAIM = "roles"

private fun Jwt.realmRoles(): List<String> {
    return when (val roles = getClaimAsMap(REALM_ACCESS_CLAIM)?.get(ROLES_CLAIM)) {
        is List<*> -> roles.filterIsInstance<String>()
        else -> emptyList()
    }
}
