package vn.id.tozydev.dokja.backend.security

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SecurityTestController {

    @GetMapping("/api/v1/me") fun me(): Map<String, String> = mapOf("status" to "ok")

    @GetMapping("/api/admin/test")
    fun adminTest(): Map<String, String> = mapOf("status" to "admin-ok")
}
