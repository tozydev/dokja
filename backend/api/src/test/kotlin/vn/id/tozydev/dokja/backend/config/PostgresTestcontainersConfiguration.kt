package vn.id.tozydev.dokja.backend.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class PostgresTestcontainersConfiguration {

    @Bean
    @ServiceConnection
    fun postgres(): PostgreSQLContainer =
        PostgreSQLContainer(postgresDockerImage)
            .withDatabaseName("dokja")
            .withUsername("dokja")
            .withPassword("dokja")

    companion object {
        private val postgresDockerImage = DockerImageName.parse("postgres:18-alpine")
    }
}
