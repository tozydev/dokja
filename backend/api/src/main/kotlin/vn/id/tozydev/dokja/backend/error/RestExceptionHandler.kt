package vn.id.tozydev.dokja.backend.error

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

/**
 * Global REST exception handling for the Dokja API.
 *
 * Extending [ResponseEntityExceptionHandler] makes Spring Boot's default order-0 problem-details
 * handler back off, so all Spring MVC exceptions are rendered here as RFC 9457 responses.
 * [DomainException] subclasses are handled through the inherited `ErrorResponse` path.
 */
@RestControllerAdvice
class RestExceptionHandler(private val problemDetailDecorator: ProblemDetailDecorator) :
    ResponseEntityExceptionHandler() {

    override fun createResponseEntity(
        body: Any?,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        if (body is ProblemDetail && request is ServletWebRequest) {
            val response = request.response
            if (response != null) {
                problemDetailDecorator.decorate(body, response)
            }
        }
        return super.createResponseEntity(body, headers, statusCode, request)
    }

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        ex.body.setProperty(ProblemDetailDecorator.MEMBER_ERRORS, ex.toValidationErrors())
        return super.handleMethodArgumentNotValid(ex, headers, status, request)
    }

    override fun handleHandlerMethodValidationException(
        ex: HandlerMethodValidationException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        ex.body.setProperty(ProblemDetailDecorator.MEMBER_ERRORS, ex.toValidationErrors())
        return super.handleHandlerMethodValidationException(ex, headers, status, request)
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception, request: WebRequest): ResponseEntity<Any>? {
        logger.error("Unhandled exception", ex)
        val problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        problem.title = "Internal Server Error"
        problem.detail = "An unexpected error occurred."
        return handleExceptionInternal(
            ex,
            problem,
            HttpHeaders.EMPTY,
            HttpStatus.INTERNAL_SERVER_ERROR,
            request,
        )
    }
}
