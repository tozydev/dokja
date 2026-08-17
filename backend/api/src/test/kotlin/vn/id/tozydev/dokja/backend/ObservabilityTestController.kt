package vn.id.tozydev.dokja.backend

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ObservabilityTestController {
    @GetMapping("/api/v1/observability-ping")
    fun ping(): Map<String, String> = mapOf("status" to "pong")
}
