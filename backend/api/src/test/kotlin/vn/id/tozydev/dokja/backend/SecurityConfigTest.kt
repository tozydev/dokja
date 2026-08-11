package vn.id.tozydev.dokja.backend

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureMockMvc
class SecurityConfigTest : IntegrationTestBase() {

    @Autowired private lateinit var mockMvc: MockMvc

    @Test
    fun `public endpoint permits unauthenticated request`() {
        mockMvc
            .perform(get("/api/v1/public/hello"))
            .andExpect(status().isOk)
            .andExpect(
                jsonPath("$.message").value("Public endpoint accessible without authentication")
            )
    }

    @Test
    fun `protected endpoint denies unauthenticated request`() {
        mockMvc.perform(get("/api/v1/user/me")).andExpect(status().isUnauthorized)
    }

    @Test
    fun `protected endpoint permits authenticated JWT request`() {
        mockMvc
            .perform(
                get("/api/v1/user/me")
                    .with(jwt().jwt { it.subject("user-123").claim("email", "user@dokja.vn") })
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.subject").value("user-123"))
            .andExpect(jsonPath("$.email").value("user@dokja.vn"))
    }
}
