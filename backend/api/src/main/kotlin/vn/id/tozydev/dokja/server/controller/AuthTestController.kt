package vn.id.tozydev.dokja.server.controller

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class AuthTestController {

    @GetMapping("/public/hello")
    fun publicHello(): Map<String, String> {
        return mapOf("message" to "Public endpoint accessible without authentication")
    }

    @GetMapping("/user/me")
    fun currentUser(@AuthenticationPrincipal jwt: Jwt): Map<String, Any?> {
        return mapOf(
            "subject" to jwt.subject,
            "claims" to jwt.claims,
            "email" to jwt.getClaimAsString("email"),
            "preferredUsername" to jwt.getClaimAsString("preferred_username"),
        )
    }
}
