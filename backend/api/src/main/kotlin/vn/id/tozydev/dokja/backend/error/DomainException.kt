package vn.id.tozydev.dokja.backend.error

import org.springframework.http.HttpStatus
import org.springframework.web.ErrorResponseException

/** Marker interface for domain error codes. */
interface DomainErrorCode

/**
 * Base class for domain/business exceptions. Automatically handles by the [RestExceptionHandler]
 * and rendered as RFC 9457 responses with the `code` extension. Subclass it per use case with the
 * HTTP status, title,detail and a dedicated [DomainErrorCode] marker:
 * ```
 * data object WalletInsufficientBalance : ErrorCode
 *
 * class WalletInsufficientBalanceException :
 *     ApiException(
 *         errorCode = WalletInsufficientBalance,
 *         status = HttpStatus.CONFLICT,
 *         title = "Insufficient balance",
 *         detail = "Your balance is not enough",
 *     )
 * ```
 *
 * The serialized `code` extension (`wallet_insufficient_balance`) is derived from the
 * [DomainErrorCode] implementation's class name in lower snake case.
 */
abstract class DomainException(
    val errorCode: DomainErrorCode,
    val status: HttpStatus,
    val title: String,
    val detail: String,
    cause: Throwable? = null,
) :
    ErrorResponseException(
        status,
        cause,
    ) {
    init {
        setTitle(title)
        setDetail(detail)
        body.setProperty(ProblemDetailDecorator.MEMBER_CODE, errorCode.toSerializedCode())
    }
}

private val pascalCaseTarget = Regex("([a-z0-9])([A-Z])")
private const val LOWER_SNAKE_CASE_REPLACEMENT = "$1_$2"

internal fun DomainErrorCode.toSerializedCode(): String =
    (this::class.simpleName ?: "domain_error")
        .replace(pascalCaseTarget, LOWER_SNAKE_CASE_REPLACEMENT)
        .lowercase()
