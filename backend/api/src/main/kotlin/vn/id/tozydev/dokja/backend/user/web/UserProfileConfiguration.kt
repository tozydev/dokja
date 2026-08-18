package vn.id.tozydev.dokja.backend.user.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class UserProfileConfiguration {

    @Bean fun keycloakRestClient(): RestClient = RestClient.create()
}
