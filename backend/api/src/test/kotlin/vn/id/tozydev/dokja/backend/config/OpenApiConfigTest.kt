package vn.id.tozydev.dokja.backend.config

import io.swagger.v3.oas.models.OpenAPI
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig

@SpringJUnitConfig(OpenApiConfig::class)
class OpenApiConfigTest {

    @Autowired private lateinit var openApi: OpenAPI

    @Test
    fun `spec metadata is configured`() {
        assertNotNull(openApi.info.title)
        assertNotNull(openApi.info.version)
    }

    @Test
    fun `bearer security scheme is configured globally`() {
        assertThat(openApi.components.securitySchemes[OpenApiConfig.BEARER_AUTH_SCHEME])
            .isNotNull
            .satisfies({ scheme ->
                assertEquals("bearer", scheme!!.scheme)
                assertEquals("JWT", scheme.bearerFormat)
            })
    }
}
