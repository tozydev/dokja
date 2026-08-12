package vn.id.tozydev.dokja.backend.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun dokjaOpenApi() =
        OpenAPI().apply {
            info = Info().title("Dokja API").description("Dokja platform API").version("v1")
            addSecurityItem(SecurityRequirement().addList(BEARER_AUTH_SCHEME))
            components =
                Components()
                    .addSecuritySchemes(
                        BEARER_AUTH_SCHEME,
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description(
                                "IdP-issued JWT token for authentication and authorization"
                            ),
                    )
        }

    companion object {
        internal const val BEARER_AUTH_SCHEME = "bearerAuth"
    }
}
