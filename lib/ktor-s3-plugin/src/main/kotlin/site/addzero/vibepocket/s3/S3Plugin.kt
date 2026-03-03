package site.addzero.vibepocket.s3

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.util.*
import org.koin.core.annotation.Single
import org.koin.ktor.ext.getKoin
import site.addzero.starter.AppStarter
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

val S3ServiceKey = AttributeKey<S3Service>("S3Service")

/**
 * S3 自动引导实现类。符合 AppStarter 接口，通过 Koin 自动发现。
 */
@Single
class S3Starter : AppStarter {
    override val order: Int get() = 60

    override fun Application.enable(): Boolean {
        return environment.config.propertyOrNull("s3.enabled")?.getAs<Boolean>() ?: true
    }

    override fun Application.onInstall() {
        val s3ConfigSection = environment.config.config("s3") ?: return
        install(createApplicationPlugin(name = "S3AutoConfiguration") {
            val s3Config = S3Config(
                endpoint = s3ConfigSection.propertyOrNull("endpoint")?.getString() ?: "https://s3.cstcloud.cn",
                region = s3ConfigSection.propertyOrNull("region")?.getString() ?: "us-east-1",
                bucket = s3ConfigSection.propertyOrNull("bucket")?.getString() ?: "af466fd92b0146ccbfb40cf590c912a0",
                accessKey = s3ConfigSection.propertyOrNull("accessKey")?.getString() ?: "",
                secretKey = s3ConfigSection.propertyOrNull("secretKey")?.getString() ?: ""
            )

            val credentials = AwsBasicCredentials.create(s3Config.accessKey, s3Config.secretKey)
            val s3Client = S3Client.builder().endpointOverride(URI.create(s3Config.endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials)).region(Region.of(s3Config.region))
                .forcePathStyle(true).build()

            val s3Service = S3Service(s3Client, s3Config)

            application.attributes.put(S3ServiceKey, s3Service)
            application.getKoin().declare(s3Service)
            application.getKoin().declare(s3Config)

            application.environment.monitor.subscribe(ApplicationStopped) {
                s3Client.close()
            }
        })
    }
}
