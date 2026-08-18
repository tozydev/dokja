package vn.id.tozydev.dokja.backend.config

import java.time.Clock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ClockConfiguration {

    @Bean fun clock(): Clock = Clock.systemUTC()
}
