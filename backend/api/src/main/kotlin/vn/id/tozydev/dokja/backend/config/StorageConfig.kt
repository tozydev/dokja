package vn.id.tozydev.dokja.backend.config

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.smithy.kotlin.runtime.net.url.Url
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(S3Properties::class)
class StorageConfig {

    @Bean
    fun s3Client(properties: S3Properties) = S3Client {
        region = properties.region
        endpointUrl = Url.parse(properties.endpoint)
        forcePathStyle = true
        credentialsProvider = StaticCredentialsProvider {
            accessKeyId = properties.accessKey
            secretAccessKey = properties.secretKey
        }
    }
}

@ConfigurationProperties(prefix = "app.storage.s3")
data class S3Properties(
    val endpoint: String,
    val region: String,
    val accessKey: String,
    val secretKey: String,
)
