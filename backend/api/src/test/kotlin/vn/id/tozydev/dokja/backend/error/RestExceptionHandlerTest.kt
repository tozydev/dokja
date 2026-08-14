package vn.id.tozydev.dokja.backend.error

import java.util.regex.Pattern
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.matchesPattern
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(
    TestErrorController::class,
    excludeAutoConfiguration = [OAuth2ResourceServerAutoConfiguration::class],
)
@Import(ProblemDetailDecorator::class, TraceIdResolverAutoConfiguration::class)
@AutoConfigureMockMvc(addFilters = false)
class RestExceptionHandlerTest {

    @Autowired private lateinit var mockMvc: MockMvc

    private val traceIdPattern: Pattern = Pattern.compile("[0-9a-f]{32}")
    private val timestampPattern: Pattern =
        Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z$")

    @Test
    fun `should return validation problem with field errors and no code`() {
        mockMvc
            .perform(
                post("/test/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name": ""}""")
            )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").isNotEmpty)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.timestamp").value(matchesPattern(timestampPattern)))
            .andExpect(jsonPath("$.type").doesNotExist())
            .andExpect(jsonPath("$.code").doesNotExist())
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("name"))
            .andExpect(jsonPath("$.errors[0].message").isNotEmpty)
            .andExpect(jsonPath("$.errors[0].code").doesNotExist())
            .andExpect(
                header()
                    .string(
                        ProblemDetailDecorator.HEADER_X_TRACE_ID,
                        matchesPattern(traceIdPattern),
                    )
            )
    }

    @Test
    fun `should return request param validation problem with field errors`() {
        mockMvc
            .perform(get("/test/param-validate").param("page", "0"))
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").isNotEmpty)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.code").doesNotExist())
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].parameter").value("page"))
            .andExpect(jsonPath("$.errors[0].message").isNotEmpty)
            .andExpect(jsonPath("$.errors[0].code").doesNotExist())
    }

    @Test
    fun `should return domain problem with code extension`() {
        mockMvc
            .perform(get("/test/domain/1"))
            .andExpect(status().isConflict)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Insufficient balance"))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value("Your balance is not enough for this purchase"))
            .andExpect(jsonPath("$.code").value("test_domain_error"))
            .andExpect(jsonPath("$.errors").doesNotExist())
            .andExpect(
                header()
                    .string(
                        ProblemDetailDecorator.HEADER_X_TRACE_ID,
                        matchesPattern(traceIdPattern),
                    )
            )
    }

    @Test
    fun `should return bad request problem for type mismatch without errors`() {
        mockMvc
            .perform(get("/test/type-mismatch/not-a-uuid"))
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.code").doesNotExist())
            .andExpect(jsonPath("$.errors").doesNotExist())
            .andExpect(
                header()
                    .string(
                        ProblemDetailDecorator.HEADER_X_TRACE_ID,
                        matchesPattern(traceIdPattern),
                    )
            )
    }

    @Test
    fun `should return not found problem for unknown route without code`() {
        mockMvc
            .perform(get("/test/nope"))
            .andExpect(status().isNotFound)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").isNotEmpty)
            .andExpect(jsonPath("$.code").doesNotExist())
            .andExpect(
                header()
                    .string(
                        ProblemDetailDecorator.HEADER_X_TRACE_ID,
                        matchesPattern(traceIdPattern),
                    )
            )
    }

    @Test
    fun `should return method not allowed problem with allow header`() {
        mockMvc
            .perform(post("/test/domain/1"))
            .andExpect(status().isMethodNotAllowed)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.code").doesNotExist())
            .andExpect(header().string("Allow", containsString("GET")))
    }

    @Test
    fun `should return 500 without leaking internals for unexpected exception`() {
        mockMvc
            .perform(get("/test/boom"))
            .andExpect(status().isInternalServerError)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Internal Server Error"))
            .andExpect(jsonPath("$.detail").value("An unexpected error occurred."))
            .andExpect(jsonPath("$.detail").value(not(containsString("secret internal detail"))))
            .andExpect(jsonPath("$.code").doesNotExist())
            .andExpect(
                header()
                    .string(
                        ProblemDetailDecorator.HEADER_X_TRACE_ID,
                        matchesPattern(traceIdPattern),
                    )
            )
    }

    @Test
    fun `should not set trace header on success response`() {
        mockMvc
            .perform(get("/test/ok"))
            .andExpect(status().isOk)
            .andExpect(header().doesNotExist(ProblemDetailDecorator.HEADER_X_TRACE_ID))
    }
}
