package vn.id.tozydev.dokja.backend.error

import jakarta.servlet.http.HttpServletResponse
import java.net.URI
import java.time.Instant
import org.springframework.http.ProblemDetail
import org.springframework.stereotype.Component
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.method.annotation.HandlerMethodValidationException

/**
 * Decorates [ProblemDetail] responses with the Dokja error format extensions and sets the
 * `X-Trace-Id` response header and optionally the `instance` URI.
 */
@Component
class ProblemDetailDecorator(private val traceIdResolver: TraceIdResolver) {
    fun decorate(
        body: ProblemDetail,
        response: HttpServletResponse,
        instance: URI? = null,
    ): ProblemDetail {
        body.setProperty(MEMBER_TIMESTAMP, Instant.now().toString())
        response.setHeader(HEADER_X_TRACE_ID, traceIdResolver.currentTraceId())
        if (instance != null) {
            body.instance = instance
        }
        return body
    }

    companion object {

        const val MEMBER_TIMESTAMP = "timestamp"
        const val MEMBER_CODE = "code"
        const val MEMBER_ERRORS = "errors"

        const val HEADER_X_TRACE_ID = "X-Trace-Id"
    }
}

internal fun HandlerMethodValidationException.toValidationErrors(): List<Map<String, String>> =
    parameterValidationResults.flatMap { result ->
        result.resolvableErrors.map { error ->
            mapOf(
                "parameter" to result.methodParameter.parameterName.orEmpty(),
                "message" to (error.defaultMessage ?: "Invalid value"),
            )
        }
    }

internal fun MethodArgumentNotValidException.toValidationErrors(): List<Map<String, String>> =
    bindingResult.fieldErrors.map { violation ->
        mapOf(
            "field" to violation.field,
            "message" to (violation.defaultMessage ?: "Invalid value"),
        )
    }
