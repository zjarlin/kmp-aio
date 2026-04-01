package site.addzero.kcloud.ui

import org.koin.core.annotation.Module
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Single
import org.koin.core.annotation.ComponentScan
import site.addzero.core.network.ApiClientSpi
import site.addzero.core.network.ApiClients
import site.addzero.core.network.HttpClientFactory
import site.addzero.generated.RouteKeys
import site.addzero.kcloud.ui.app.KCloudRouteCatalog

@KoinApplication(
    modules = [
        KCloudUiScanKoinModule::class,
        KCloudUiSupportKoinModule::class,
    ],
)
object KCloudUiKoinApplication

@Module
@ComponentScan("site.addzero")
class KCloudUiScanKoinModule {
    @Single
    fun provideRouteCatalog(): KCloudRouteCatalog {
        return KCloudRouteCatalog(RouteKeys.allMeta)
    }
}

@Module
class KCloudUiSupportKoinModule {
    @Single
    fun provideHttpClientFactory(): HttpClientFactory {
        return HttpClientFactory()
    }

    @Single
    fun provideApiClients(
        apiClientSpis: List<ApiClientSpi>,
        httpClientFactory: HttpClientFactory,
    ): ApiClients {
        return ApiClients(
            apiClientSpis = apiClientSpis,
            httpClientFactory = httpClientFactory,
        )
    }
}
