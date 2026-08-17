package vn.id.tozydev.dokja.backend.security

import org.junit.jupiter.api.Test
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
class SecurityConfigTest {

    @Autowired private lateinit var mockMvc: MockMvc

    @Test
    fun `public endpoint permits unauthenticated request`() {
        mockMvc.perform(get("/api/v1/public/ping")).andExpect(status().isOk)
    }

    @Test
    fun `protected endpoint denies unauthenticated request`() {
        mockMvc.perform(get("/api/v1/me")).andExpect(status().isUnauthorized)
    }

    @Test
    fun `protected endpoint permits authenticated request`() {
        mockMvc
            .perform(get("/api/v1/me").with(jwt().jwt { it.subject("user-123") }))
            .andExpect(status().isOk)
    }

    @Test
    fun `moderator endpoint denies user without role`() {
        mockMvc
            .perform(
                get("/api/v1/moderator/hello")
                    .with(jwt().authorities(SimpleGrantedAuthority("ROLE_USER")))
            )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `moderator endpoint permits user with moderator role`() {
        mockMvc
            .perform(
                get("/api/v1/moderator/hello")
                    .with(jwt().authorities(SimpleGrantedAuthority("ROLE_MODERATOR")))
            )
            .andExpect(status().isOk)
    }

    @Test
    fun `staff endpoint permits user with operation admin role`() {
        mockMvc
            .perform(
                get("/api/v1/staff/hello")
                    .with(jwt().authorities(SimpleGrantedAuthority("ROLE_OPERATION_ADMIN")))
            )
            .andExpect(status().isOk)
    }

    @Test
    fun `staff endpoint denies user without required role`() {
        mockMvc
            .perform(
                get("/api/v1/staff/hello")
                    .with(jwt().authorities(SimpleGrantedAuthority("ROLE_USER")))
            )
            .andExpect(status().isForbidden)
    }
}
