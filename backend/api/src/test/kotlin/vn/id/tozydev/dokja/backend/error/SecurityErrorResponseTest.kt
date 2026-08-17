package vn.id.tozydev.dokja.backend.error

import java.util.regex.Pattern
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.matchesPattern
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import vn.id.tozydev.dokja.backend.security.SecurityConfig

@WebMvcTest(
    properties =
        [
            "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9000/realms/dokja-dev"
        ]
)
@Import(
    SecurityConfig::class,
    SecurityErrorResponseConfig::class,
    ProblemDetailDecorator::class,
    TraceIdResolverAutoConfiguration::class,
    SecurityErrorResponseTest.AdminChainConfig::class,
)
@AutoConfigureMockMvc
@AutoConfigureJson
class SecurityErrorResponseTest {

    @Autowired private lateinit var mockMvc: MockMvc

    private val traceIdPattern: Pattern = Pattern.compile("[0-9a-f]{32}")

    @Test
    fun `should return 401 problem with standard headers for unauthenticated request`() {
        mockMvc
            .perform(get("/test/security/me"))
            .andExpect(status().isUnauthorized)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").isNotEmpty)
            .andExpect(jsonPath("$.detail").isNotEmpty)
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.instance").value("/test/security/me"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.type").doesNotExist())
            .andExpect(jsonPath("$.code").doesNotExist())
            .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("Bearer")))
            .andExpect(
                header()
                    .string(
                        ProblemDetailDecorator.HEADER_X_TRACE_ID,
                        matchesPattern(traceIdPattern),
                    )
            )
    }

    @Test
    fun `should return 403 problem with standard headers for insufficient authority`() {
        mockMvc
            .perform(
                get("/admin/secret").with(jwt().authorities(SimpleGrantedAuthority("ROLE_USER")))
            )
            .andExpect(status().isForbidden)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").isNotEmpty)
            .andExpect(jsonPath("$.detail").isNotEmpty)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.instance").value("/admin/secret"))
            .andExpect(jsonPath("$.code").doesNotExist())
            .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("no-store")))
            .andExpect(
                header()
                    .string(
                        ProblemDetailDecorator.HEADER_X_TRACE_ID,
                        matchesPattern(traceIdPattern),
                    )
            )
    }

    /**
     * Test-only security chain adding a role-protected surface so the access denied handler can be
     * exercised; the real [SecurityConfig] chain handles everything else.
     */
    @Configuration(proxyBeanMethods = false)
    class AdminChainConfig {

        @Bean
        @Order(1)
        fun adminSecurityFilterChain(
            http: HttpSecurity,
            problemDetailAuthenticationEntryPoint: AuthenticationEntryPoint,
            problemDetailAccessDeniedHandler: AccessDeniedHandler,
        ): SecurityFilterChain {
            http {
                securityMatcher("/admin/**")
                csrf { disable() }
                sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
                authorizeHttpRequests { authorize(anyRequest, hasRole("ADMIN")) }
                oauth2ResourceServer {
                    jwt {}
                    authenticationEntryPoint = problemDetailAuthenticationEntryPoint
                }
                exceptionHandling { accessDeniedHandler = problemDetailAccessDeniedHandler }
            }
            return http.build()
        }
    }
}
