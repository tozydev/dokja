package vn.id.tozydev.dokja.backend.security

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import vn.id.tozydev.dokja.backend.error.ProblemDetailDecorator
import vn.id.tozydev.dokja.backend.error.SecurityErrorResponseConfig
import vn.id.tozydev.dokja.backend.error.TraceIdResolverAutoConfiguration

@WebMvcTest(
    properties =
        [
            "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9000/realms/dokja-dev"
        ]
)
@Import(
    SecurityConfig::class,
    SecurityErrorResponseConfig::class,
    ProblemDetailDecorator::class,
    TraceIdResolverAutoConfiguration::class,
)
@AutoConfigureMockMvc
@AutoConfigureJson
class SecurityConfigTest(@Autowired private val mockMvc: MockMvc) {
    @Test
    fun `denies unauthenticated request to protected endpoint`() {
        mockMvc.perform(get("/api/v1/me")).andExpect(status().isUnauthorized)
    }

    @Test
    fun `permits authenticated request to protected endpoint`() {
        mockMvc
            .perform(get("/api/v1/me").with(jwt().jwt { it.subject("user-123") }))
            .andExpect(status().isOk)
    }

    @Test
    fun `denies unauthenticated request to admin endpoint`() {
        mockMvc.perform(get("/api/admin/test")).andExpect(status().isUnauthorized)
    }

    @ParameterizedTest
    @MethodSource("nonAdminAuthorities")
    fun `denies non-admin role to admin endpoint`(authority: SimpleGrantedAuthority) {
        mockMvc
            .perform(get("/api/admin/test").with(jwt().authorities(authority)))
            .andExpect(status().isForbidden)
    }

    @ParameterizedTest
    @MethodSource("adminAuthorities")
    fun `permits admin role to admin endpoint`(authority: SimpleGrantedAuthority) {
        mockMvc
            .perform(get("/api/admin/test").with(jwt().authorities(authority)))
            .andExpect(status().isOk)
    }

    companion object {
        @JvmStatic
        fun nonAdminAuthorities() =
            Role.entries.filter { !it.isAdmin }.map { SimpleGrantedAuthority(it.authority) }

        @JvmStatic
        fun adminAuthorities() = Role.adminRoles.map { SimpleGrantedAuthority(it.authority) }
    }
}
