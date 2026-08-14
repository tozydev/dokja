package vn.id.tozydev.dokja.backend.config

import io.swagger.v3.oas.models.OpenAPI
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class OpenApiConfigTest {

    private val openApi = OpenAPI().also { OpenApiConfig().customise(it) }

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

    @Test
    fun `error code schema is registered for the generated client`() {
        assertThat(openApi.components.schemas[OpenApiConfig.ERROR_CODE_SCHEMA])
            .isNotNull
            .satisfies({ schema ->
                assertEquals("string", schema!!.type)
                assertNotNull(schema.enum)
            })
    }
}
