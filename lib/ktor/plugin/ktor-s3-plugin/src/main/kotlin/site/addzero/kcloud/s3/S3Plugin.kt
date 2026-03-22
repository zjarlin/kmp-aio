package site.addzero.kcloud.s3

import io.ktor.server.application.*
import io.ktor.server.config.*
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Property
import org.koin.core.annotation.Single
import org.koin.ktor.ext.getKoin
import site.addzero.starter.AppStarter
import site.addzero.starter.effectiveConfig
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

private const val APPLICATION_CONFIG_PROPERTY = "vibepocket.applicationConfig"

@Module
@Configuration("vibepocket")
@ComponentScan("site.addzero.vibepocket.s3")
class S3KoinModule {
    @Single
    fun provideS3Config(
        @Property(APPLICATION_CONFIG_PROPERTY)
        config: ApplicationConfig,
    ): S3Config {
        val section = runCatching { config.config("s3") }.getOrNull()
        return S3Config(
            endpoint = section?.propertyOrNull("endpoint")?.getString() ?: "https://s3.cstcloud.cn",
            region = section?.propertyOrNull("region")?.getString() ?: "us-east-1",
            bucket = section?.propertyOrNull("bucket")?.getString() ?: "af466fd92b0146ccbfb40cf590c912a0",
            accessKey = section?.propertyOrNull("accessKey")?.getString() ?: "",
            secretKey = section?.propertyOrNull("secretKey")?.getString() ?: "",
        )
    }

    @Single
    fun provideS3Client(config: S3Config): S3Client {
        val credentials = AwsBasicCredentials.create(config.accessKey, config.secretKey)
        return S3Client.builder()
            .endpointOverride(URI.create(config.endpoint))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .region(Region.of(config.region))
            .forcePathStyle(true)
            .build()
    }
}

/**
 * S3 自动引导实现类。符合 AppStarter 接口，通过 Koin 自动发现。
 */
@Named("s3Starter")
@Single(binds = [AppStarter::class])
class S3Starter : AppStarter {
    override val order: Int get() = 60

    override fun Application.enable(): Boolean {
        val s3Config = runCatching { effectiveConfig().config("s3") }.getOrNull()
        return s3Config?.propertyOrNull("enabled")?.getString()?.toBoolean() != false
    }

    override fun Application.onInstall() {
        val s3Config = getKoin().get<S3Config>()
        if (s3Config.accessKey.isBlank() || s3Config.secretKey.isBlank()) {
            log.warn("Skipping S3 starter because accessKey/secretKey are blank")
            return
        }

        install(createApplicationPlugin(name = "S3AutoConfiguration") {
            val s3Client = application.getKoin().get<S3Client>()

            application.monitor.subscribe(ApplicationStopping) {
                s3Client.close()
            }
        })
    }
}
