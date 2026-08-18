package vn.id.tozydev.dokja.backend.user.web

import java.time.LocalDate
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import vn.id.tozydev.dokja.backend.error.ProblemDetailDecorator
import vn.id.tozydev.dokja.backend.error.TraceIdResolverAutoConfiguration
import vn.id.tozydev.dokja.backend.user.AgeClassification
import vn.id.tozydev.dokja.backend.user.AgeClassifier
import vn.id.tozydev.dokja.backend.user.error.KeycloakUnavailableException
import vn.id.tozydev.dokja.backend.user.web.KeycloakUserInfoClient.UserInfo

@WebMvcTest(
    UserController::class,
    properties =
        [
            "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9000/realms/dokja-dev"
        ],
)
@Import(ProblemDetailDecorator::class, TraceIdResolverAutoConfiguration::class)
@AutoConfigureJson
class UserControllerTest(@Autowired private val mockMvc: MockMvc) {

    @MockitoBean private lateinit var keycloakClient: KeycloakUserInfoClient

    @MockitoBean private lateinit var ageClassifier: AgeClassifier

    @Test
    fun `returns user profile with birthdate and age classification`() {
        val userInfo =
            UserInfo(
                sub = "881edbc9-8711-4bca-a14e-85829b264620",
                name = "System Admin",
                given_name = "System",
                family_name = "Admin",
                preferred_username = "system_admin",
                email = "system-admin@dokja",
                email_verified = true,
                birthdate = LocalDate.of(2000, 6, 1),
            )

        Mockito.`when`(keycloakClient.getUserInfo(anyNonNull())).thenReturn(userInfo)
        Mockito.`when`(ageClassifier.classify(LocalDate.of(2000, 6, 1)))
            .thenReturn(AgeClassification.R_18)
        Mockito.`when`(ageClassifier.computeAge(LocalDate.of(2000, 6, 1))).thenReturn(26)

        mockMvc
            .perform(
                get("/api/v1/user/profile")
                    .with(jwt().jwt { it.subject("881edbc9-8711-4bca-a14e-85829b264620") })
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.sub").value("881edbc9-8711-4bca-a14e-85829b264620"))
            .andExpect(jsonPath("$.name").value("System Admin"))
            .andExpect(jsonPath("$.givenName").value("System"))
            .andExpect(jsonPath("$.familyName").value("Admin"))
            .andExpect(jsonPath("$.preferredUsername").value("system_admin"))
            .andExpect(jsonPath("$.email").value("system-admin@dokja"))
            .andExpect(jsonPath("$.emailVerified").value(true))
            .andExpect(jsonPath("$.birthdate").value("2000-06-01"))
            .andExpect(jsonPath("$.age").value(26))
            .andExpect(jsonPath("$.ageClassification").value("r_18"))
    }

    @Test
    fun `returns null age fields when birthdate is not provided`() {
        val userInfo =
            UserInfo(
                sub = "881edbc9-8711-4bca-a14e-85829b264620",
                name = "System Admin",
                given_name = "System",
                family_name = "Admin",
                preferred_username = "system_admin",
                email = "system-admin@dokja",
                email_verified = true,
                birthdate = null,
            )

        Mockito.`when`(keycloakClient.getUserInfo(anyNonNull())).thenReturn(userInfo)
        Mockito.`when`(ageClassifier.classify(null)).thenReturn(AgeClassification.P)

        mockMvc
            .perform(
                get("/api/v1/user/profile")
                    .with(jwt().jwt { it.subject("881edbc9-8711-4bca-a14e-85829b264620") })
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.birthdate").isEmpty)
            .andExpect(jsonPath("$.age").isEmpty)
            .andExpect(jsonPath("$.ageClassification").value("p"))
    }

    @Test
    fun `denies unauthenticated request`() {
        mockMvc.perform(get("/api/v1/user/profile")).andExpect(status().isUnauthorized)
    }

    @Test
    fun `returns 503 when keycloak is unavailable`() {
        Mockito.`when`(keycloakClient.getUserInfo(anyNonNull()))
            .thenThrow(KeycloakUnavailableException())

        mockMvc
            .perform(
                get("/api/v1/user/profile")
                    .with(jwt().jwt { it.subject("881edbc9-8711-4bca-a14e-85829b264620") })
            )
            .andExpect(status().isServiceUnavailable)
            .andExpect(jsonPath("$.code").value("keycloak_unavailable"))
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> anyNonNull(): T {
        Mockito.any<T>()
        return null as T
    }
}
