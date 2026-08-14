package vn.id.tozydev.dokja.backend.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AssignableTypeFilter
import org.springframework.stereotype.Component
import vn.id.tozydev.dokja.backend.DokjaBackendApplication
import vn.id.tozydev.dokja.backend.error.DomainErrorCode
import vn.id.tozydev.dokja.backend.error.toSerializedCode

@Component
class OpenApiConfig : OpenApiCustomizer {

    override fun customise(openApi: OpenAPI) {
        openApi.info = Info().title("Dokja API").description("Dokja platform API").version("v1")
        openApi.addSecurityItem(SecurityRequirement().addList(BEARER_AUTH_SCHEME))

        val components = openApi.components ?: Components()
        components.addSecuritySchemes(BEARER_AUTH_SCHEME, securityScheme())
        components.addSchemas(ERROR_CODE_SCHEMA, errorCodeSchema())

        openApi.components = components
    }

    private fun securityScheme(): SecurityScheme =
        SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description("IdP-issued JWT token for authentication and authorization")

    private fun errorCodeSchema(): Schema<String> =
        Schema<String>().apply {
            type = "string"
            description = "Stable machine-readable error code of a domain error"
            enum = scanErrorCodes()
        }

    private fun scanErrorCodes(): List<String> {
        val scanner = ClassPathScanningCandidateComponentProvider(false)
        scanner.addIncludeFilter(AssignableTypeFilter(DomainErrorCode::class.java))
        return scanner
            .findCandidateComponents(DokjaBackendApplication::class.java.packageName)
            .mapNotNull { candidate ->
                val clazz = Class.forName(candidate.beanClassName)
                if (
                    DomainErrorCode::class.java.isAssignableFrom(clazz) &&
                        clazz != DomainErrorCode::class.java
                ) {
                    (clazz.kotlin.objectInstance as? DomainErrorCode)?.toSerializedCode()
                } else {
                    null
                }
            }
            .sorted()
    }

    companion object {
        const val BEARER_AUTH_SCHEME = "bearerAuth"
        const val ERROR_CODE_SCHEMA = "ErrorCode"
    }
}
