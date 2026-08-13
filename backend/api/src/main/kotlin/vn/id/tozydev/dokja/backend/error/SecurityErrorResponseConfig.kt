package vn.id.tozydev.dokja.backend.error

import jakarta.servlet.http.HttpServletResponse
import java.net.URI
import org.springframework.context.MessageSource
import org.springframework.context.MessageSourceAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.web.ErrorResponse
import tools.jackson.databind.json.JsonMapper

/** RFC 9457 problem responses for the security filter chain (401/403). */
@Configuration(proxyBeanMethods = false)
class SecurityErrorResponseConfig(
    private val problemDetailJsonMapper: JsonMapper,
    private val problemDetailDecorator: ProblemDetailDecorator,
) : MessageSourceAware {
    private val bearerTokenAuthenticationEntryPoint = BearerTokenAuthenticationEntryPoint()
    private val bearerTokenAccessDeniedHandler = BearerTokenAccessDeniedHandler()

    private var _messageSource: MessageSource? = null
    val messageSource: MessageSource?
        get() = _messageSource

    override fun setMessageSource(messageSource: MessageSource) {
        this._messageSource = messageSource
    }

    @Bean
    fun problemDetailAuthenticationEntryPoint(): AuthenticationEntryPoint =
        AuthenticationEntryPoint { request, response, ex ->
            bearerTokenAuthenticationEntryPoint.commence(request, response, ex)
            writeProblem(
                ex = ex,
                response = response,
                instance = URI.create(request.requestURI),
                status = HttpStatus.UNAUTHORIZED,
                title = "Unauthorized",
                detail = "Authentication is required",
            )
        }

    @Bean
    fun problemDetailAccessDeniedHandler(): AccessDeniedHandler =
        AccessDeniedHandler { request, response, ex ->
            bearerTokenAccessDeniedHandler.handle(request, response, ex)
            writeProblem(
                ex = ex,
                response = response,
                instance = URI.create(request.requestURI),
                status = HttpStatus.FORBIDDEN,
                title = "Forbidden",
                detail = "Insufficient permissions",
            )
        }

    private fun writeProblem(
        ex: Throwable,
        response: HttpServletResponse,
        instance: URI,
        status: HttpStatus,
        title: String,
        detail: String,
    ) {
        response.status = status.value()
        response.contentType = MediaType.APPLICATION_PROBLEM_JSON_VALUE

        val errorResponse = ErrorResponse.builder(ex, status, detail).title(title).build()
        val body = errorResponse.updateAndGetBody(messageSource, LocaleContextHolder.getLocale())
        problemDetailDecorator.decorate(body, response, instance = instance)

        problemDetailJsonMapper.writeValue(response.writer, body)
    }
}
