package vn.id.tozydev.dokja.backend.user.web

import java.net.URI
import java.time.LocalDate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.body
import vn.id.tozydev.dokja.backend.user.error.KeycloakUnavailableException

/** Client that calls the Keycloak OIDC UserInfo endpoint to fetch the current user's profile. */
@Component
class KeycloakUserInfoClient(
    private val restClient: RestClient,
    @Value($$"${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private val issuerUri: String,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Fetches the OIDC UserInfo for the given [accessToken].
     *
     * @return [UserInfo] parsed from the JSON response
     * @throws KeycloakUnavailableException if the call fails
     */
    fun getUserInfo(accessToken: String): UserInfo {
        val uri = URI.create("$issuerUri/protocol/openid-connect/userinfo")
        return try {
            restClient
                .get()
                .uri(uri)
                .headers { it.setBearerAuth(accessToken) }
                .retrieve()
                .body<UserInfo>() ?: throw KeycloakUnavailableException()
        } catch (e: RestClientResponseException) {
            log.error("Keycloak UserInfo request failed: status=${e.statusCode}", e)
            throw KeycloakUnavailableException()
        } catch (e: Exception) {
            log.error("Keycloak UserInfo request failed", e)
            throw KeycloakUnavailableException()
        }
    }

    /**
     * OIDC UserInfo response model.
     *
     * Maps standard OIDC claims. Fields are nullable because Keycloak may not include all claims
     * depending on client scope configuration.
     */
    @Suppress("PropertyName")
    data class UserInfo(
        val sub: String,
        val name: String?,
        val given_name: String?,
        val family_name: String?,
        val preferred_username: String?,
        val email: String?,
        val email_verified: Boolean?,
        val birthdate: LocalDate?,
    )
}
