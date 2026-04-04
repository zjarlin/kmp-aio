package site.addzero.kcloud.s3

import io.ktor.server.application.*
import io.ktor.server.config.*
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.koin.ktor.ext.getKoin
import site.addzero.configcenter.ConfigCenter
import site.addzero.starter.AppStarter
import site.addzero.starter.effectiveConfig
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

@Module
@ComponentScan("site.addzero.kcloud.s3")
class S3KoinModule {
    @Single
    fun provideS3Config(
        config: ApplicationConfig,
    ): S3Config {
        val env = ConfigCenter.getEnv(config).path("s3")
        return S3Config(
            endpoint = env.string("endpoint", "https://s3.cstcloud.cn") ?: "https://s3.cstcloud.cn",
            region = env.string("region", "us-east-1") ?: "us-east-1",
            bucket = env.string("bucket", "af466fd92b0146ccbfb40cf590c912a0") ?: "af466fd92b0146ccbfb40cf590c912a0",
            accessKey = env.string("accessKey").orEmpty(),
            secretKey = env.string("secretKey").orEmpty(),
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
@Single
class S3Starter : AppStarter {
    override val order get() = 60

    override fun Application.enable(): Boolean {
        return ConfigCenter.getEnv(effectiveConfig())
            .path("s3")
            .boolean("enabled", true) != false
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
