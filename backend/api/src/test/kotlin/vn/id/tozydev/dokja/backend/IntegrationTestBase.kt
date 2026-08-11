package vn.id.tozydev.dokja.backend

import com.redis.testcontainers.RedisContainer
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@Testcontainers
@SpringBootTest
@ActiveProfiles("integration-test")
abstract class IntegrationTestBase {

    companion object {
        private val postgesDockerImage = DockerImageName.parse("postgres:18-alpine")
        private val redisDockerImage = DockerImageName.parse("redis:8-alpine")

        private val rustfsDockerImage = DockerImageName.parse("rustfs/rustfs:latest")
        private const val RUSTFS_PORT = 9000
        private const val RUSTFS_ACCESS_KEY = "dokja"
        private const val RUSTFS_SECRET_KEY = "dokja"

        @Container
        @JvmStatic
        @ServiceConnection
        val postgres: PostgreSQLContainer =
            PostgreSQLContainer(postgesDockerImage)
                .withDatabaseName("dokja")
                .withUsername("dokja")
                .withPassword("dokja")

        @Container
        @JvmStatic
        @ServiceConnection("redis")
        val redis: RedisContainer = RedisContainer(redisDockerImage)

        @Container
        @JvmStatic
        val rustfs: GenericContainer<*> =
            GenericContainer(rustfsDockerImage)
                .withEnv("RUSTFS_ACCESS_KEY", RUSTFS_ACCESS_KEY)
                .withEnv("RUSTFS_SECRET_KEY", RUSTFS_SECRET_KEY)
                .withExposedPorts(RUSTFS_PORT)

        @DynamicPropertySource
        @JvmStatic
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("app.storage.s3.endpoint") {
                "http://${rustfs.host}:${rustfs.getMappedPort(RUSTFS_PORT)}"
            }
            registry.add("app.storage.s3.region") { "vi" }
            registry.add("app.storage.s3.access-key") { RUSTFS_ACCESS_KEY }
            registry.add("app.storage.s3.secret-key") { RUSTFS_SECRET_KEY }
        }
    }
}
