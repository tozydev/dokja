package vn.id.tozydev.dokja.backend.user.web

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import vn.id.tozydev.dokja.backend.user.AgeClassifier

@RestController
@RequestMapping("/api/v1/user")
class UserController(
    private val keycloakClient: KeycloakUserInfoClient,
    private val ageClassifier: AgeClassifier,
) {

    @GetMapping("/profile")
    fun getProfile(@AuthenticationPrincipal jwt: Jwt): UserResponse {
        val userInfo = keycloakClient.getUserInfo(jwt.tokenValue)

        val age = userInfo.birthdate?.let { ageClassifier.computeAge(it) }
        val ageClassification = ageClassifier.classify(userInfo.birthdate)

        return UserResponse(
            sub = userInfo.sub,
            name = userInfo.name,
            givenName = userInfo.given_name,
            familyName = userInfo.family_name,
            preferredUsername = userInfo.preferred_username,
            email = userInfo.email,
            emailVerified = userInfo.email_verified,
            birthdate = userInfo.birthdate,
            age = age,
            ageClassification = ageClassification,
        )
    }
}
