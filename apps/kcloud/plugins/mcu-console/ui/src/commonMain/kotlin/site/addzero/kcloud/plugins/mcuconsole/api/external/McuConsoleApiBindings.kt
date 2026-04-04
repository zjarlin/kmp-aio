package site.addzero.kcloud.plugins.mcuconsole.api.external

import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.core.network.HttpClientFactory

private const val defaultBaseUrl = "http://localhost:18080/"
private const val httpClientProfile = "kcloud-mcu-console"

private object McuConsoleApiRuntime {
    @Volatile
    private var baseUrl = defaultBaseUrl

    fun configureBaseUrl(
        value: String,
    ) {
        baseUrl = normalizeBaseUrl(value)
    }

    fun createKtorfit(
        httpClientFactory: HttpClientFactory,
    ): Ktorfit {
        return Ktorfit.Builder()
            .baseUrl(baseUrl)
            .httpClient(httpClientFactory.get(httpClientProfile))
            .build()
    }

    private fun normalizeBaseUrl(
        value: String,
    ): String {
        return value.trim()
            .ifBlank { defaultBaseUrl }
            .trimEnd('/') + "/"
    }
}

fun configureMcuConsoleApis(
    baseUrl: String,
) {
    McuConsoleApiRuntime.configureBaseUrl(baseUrl)
}

@Module
class McuConsoleApiModule {
    @Single
    fun provideKtorfit(
        httpClientFactory: HttpClientFactory,
    ): Ktorfit {
        return McuConsoleApiRuntime.createKtorfit(httpClientFactory)
    }
}
