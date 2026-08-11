package vn.id.tozydev.dokja.backend

import aws.sdk.kotlin.services.s3.S3Client
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject

class DokjaBackendApplicationTests : IntegrationTestBase() {

    @Autowired private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired private lateinit var redisTemplate: StringRedisTemplate

    @Autowired private lateinit var s3Client: S3Client

    @Test
    fun `context loads and connects to postgres`() {
        assertEquals(1, jdbcTemplate.queryForObject<Int>("select 1"))
    }

    @Test
    fun `connects to redis`() {
        redisTemplate.opsForValue().set("dokja:smoke-test", "ok")
        assertEquals("ok", redisTemplate.opsForValue().get("dokja:smoke-test"))
    }

    @Test
    suspend fun `connects to rustfs`() {
        val buckets = s3Client.listBuckets().buckets
        assertNotNull(buckets)
    }
}
