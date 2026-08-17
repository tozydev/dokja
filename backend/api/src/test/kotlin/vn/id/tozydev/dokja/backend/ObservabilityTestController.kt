package vn.id.tozydev.dokja.backend

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/public")
class ObservabilityTestController {
    @GetMapping("/observability-ping") fun ping(): Map<String, String> = mapOf("status" to "pong")
}
