package vn.id.tozydev.dokja.backend.security

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class SecurityTestController {

    @GetMapping("/public/ping") fun ping(): Map<String, String> = mapOf("status" to "pong")

    @GetMapping("/me") fun me(): Map<String, String> = mapOf("status" to "ok")

    @GetMapping("/moderator/hello")
    @PreAuthorize("hasRole('${Role.Authorities.MODERATOR}')")
    fun moderatorHello(): Map<String, String> = mapOf("message" to "moderator-ok")

    @GetMapping("/staff/hello")
    @PreAuthorize(
        "hasAnyRole('${Role.Authorities.MODERATOR}', '${Role.Authorities.OPERATION_ADMIN}')"
    )
    fun staffHello(): Map<String, String> = mapOf("message" to "staff-ok")
}
