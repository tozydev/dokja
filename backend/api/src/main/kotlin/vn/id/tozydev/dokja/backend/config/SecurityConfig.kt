package vn.id.tozydev.dokja.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val environment: Environment,
    private val problemDetailAuthenticationEntryPoint: AuthenticationEntryPoint,
    private val problemDetailAccessDeniedHandler: AccessDeniedHandler,
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            cors { configurationSource = corsConfigurationSource() }
            csrf { disable() }
            sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
            authorizeHttpRequests {
                if (environment.acceptsProfiles(Profiles.of("dev"))) {
                    authorize("/v3/api-docs/**", permitAll)
                    authorize("/swagger-ui/**", permitAll)
                    authorize("/swagger-ui.html", permitAll)
                    authorize("/actuator/**", permitAll)
                }
                authorize("/api/v1/public/**", permitAll)
                authorize(anyRequest, authenticated)
            }
            oauth2ResourceServer {
                authenticationEntryPoint = problemDetailAuthenticationEntryPoint
                jwt {}
            }
            exceptionHandling { accessDeniedHandler = problemDetailAccessDeniedHandler }
        }
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOriginPatterns = listOf("http://localhost:*")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
