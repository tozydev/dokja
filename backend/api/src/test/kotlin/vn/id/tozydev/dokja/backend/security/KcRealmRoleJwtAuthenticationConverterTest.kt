package vn.id.tozydev.dokja.backend.security

import java.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.Jwt

class KcRealmRoleJwtAuthenticationConverterTest {

    private val converter = KcRealmRoleJwtAuthenticationConverter()

    @Test
    fun `should map known realm roles to ROLE_ authorities`() {
        val jwt = buildJwt(mapOf("realm_access" to mapOf("roles" to listOf("moderator", "user"))))

        val token = converter.convert(jwt)

        val authorities = token.authorities.map { it.authority }.toSet()
        assertEquals(setOf("ROLE_MODERATOR", "ROLE_USER"), authorities)
    }

    @Test
    fun `should filter unknown realm roles`() {
        val jwt =
            buildJwt(mapOf("realm_access" to mapOf("roles" to listOf("moderator", "unknown-role"))))

        val token = converter.convert(jwt)

        val authorities = token.authorities.map { it.authority }.toSet()
        assertEquals(setOf("ROLE_MODERATOR"), authorities)
    }

    @Test
    fun `should return empty authorities when realm_access is missing`() {
        val jwt = buildJwt(emptyMap())

        val token = converter.convert(jwt)

        assertTrue(token.authorities.isEmpty())
    }

    @Test
    fun `should return empty authorities when roles is not a list`() {
        val jwt = buildJwt(mapOf("realm_access" to mapOf("roles" to "moderator")))

        val token = converter.convert(jwt)

        assertTrue(token.authorities.isEmpty())
    }

    @Test
    fun `should return empty authorities when realm_access has no roles key`() {
        val jwt = buildJwt(mapOf("realm_access" to mapOf("other" to listOf("moderator"))))

        val token = converter.convert(jwt)

        assertTrue(token.authorities.isEmpty())
    }

    @Test
    fun `should always produce authenticated token`() {
        val jwt = buildJwt(emptyMap())

        val token = converter.convert(jwt)

        assertTrue(token.isAuthenticated)
        assertEquals(jwt, token.principal)
    }

    @Test
    fun `should map all role enum values`() {
        val allRoles = Role.entries.map { it.realmRole }
        val jwt = buildJwt(mapOf("realm_access" to mapOf("roles" to allRoles)))

        val token = converter.convert(jwt)

        val authorities = token.authorities.map { it.authority }.toSet()
        val expected = Role.entries.map { it.authority }.toSet()
        assertEquals(expected, authorities)
    }

    private fun buildJwt(claims: Map<String, Any>): Jwt =
        Jwt.withTokenValue("test-token")
            .header("alg", "RS256")
            .claims { it.putAll(claims) }
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build()
}
