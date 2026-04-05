package site.addzero.kcloud.bootstrap

import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.core.network.HttpClientFactory
import site.addzero.kcloud.config.KcloudFrontendRuntimeConfig

private const val KCLOUD_API_HTTP_CLIENT_PROFILE = "kcloud-api"

private object KcloudFrontendRuntimeBootstrap {
    private var runtimeConfig: KcloudFrontendRuntimeConfig? = null

    fun install(
        value: KcloudFrontendRuntimeConfig,
    ) {
        val normalized = KcloudFrontendRuntimeConfig(
            apiBaseUrl = value.normalizedApiBaseUrl(),
        )
        val installed = runtimeConfig
        if (installed == null) {
            runtimeConfig = normalized
            return
        }
        check(installed == normalized) {
            "KcloudFrontendRuntimeConfig 已初始化，不能在运行时切换。"
        }
    }

    fun requireRuntimeConfig(): KcloudFrontendRuntimeConfig {
        return requireNotNull(runtimeConfig) {
            "KcloudFrontendRuntimeConfig 尚未初始化。请在应用启动前完成前端 runtime config 装配。"
        }
    }
}

data class KcloudApiHttpClientProfile(
    val value: String = KCLOUD_API_HTTP_CLIENT_PROFILE,
)

fun bootstrapKcloudFrontendRuntimeConfig(
    runtimeConfig: KcloudFrontendRuntimeConfig,
) {
    KcloudFrontendRuntimeBootstrap.install(runtimeConfig)
}

@Module
class KcloudApiModule {
    @Single
    fun provideFrontendRuntimeConfig(): KcloudFrontendRuntimeConfig {
        return KcloudFrontendRuntimeBootstrap.requireRuntimeConfig()
    }

    @Single
    fun provideApiHttpClientProfile(): KcloudApiHttpClientProfile {
        return KcloudApiHttpClientProfile()
    }

    @Single
    fun provideKtorfit(
        httpClientFactory: HttpClientFactory,
        runtimeConfig: KcloudFrontendRuntimeConfig,
        httpClientProfile: KcloudApiHttpClientProfile,
    ): Ktorfit {
        return Ktorfit.Builder()
            .baseUrl(runtimeConfig.normalizedApiBaseUrl())
            .httpClient(httpClientFactory.get(httpClientProfile.value))
            .build()
    }
}
