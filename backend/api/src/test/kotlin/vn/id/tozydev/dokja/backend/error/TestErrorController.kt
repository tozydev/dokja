package vn.id.tozydev.dokja.backend.error

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import java.util.*
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/test")
class TestErrorController {

    data class SampleRequest(@field:NotBlank val name: String)

    @GetMapping("/ok") fun ok(): Map<String, String> = mapOf("message" to "ok")

    @PostMapping("/validate")
    fun validate(@Valid @RequestBody body: SampleRequest): Map<String, String> =
        mapOf("name" to body.name)

    @GetMapping("/param-validate")
    fun paramValidate(@RequestParam @Min(1) page: Int): Map<String, Int> = mapOf("page" to page)

    @GetMapping("/domain/{id}")
    fun domainError(@PathVariable id: String): String = throw WalletInsufficientBalanceException()

    @GetMapping("/type-mismatch/{id}")
    fun typeMismatch(@PathVariable id: UUID): Map<String, String> = mapOf("id" to id.toString())

    @GetMapping("/boom")
    fun unexpected(): String = throw IllegalStateException("secret internal detail")
}

data object TestDomainError : DomainErrorCode

class WalletInsufficientBalanceException :
    DomainException(
        errorCode = TestDomainError,
        status = HttpStatus.CONFLICT,
        title = "Insufficient balance",
        detail = "Your balance is not enough for this purchase",
    )
