package site.addzero.kcloud.bootstrap

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.core.network.ApiClientSpi
import site.addzero.core.network.ApiClients
import site.addzero.core.network.HttpClientFactory
import site.addzero.generated.RouteKeys
import site.addzero.component.chat.AddChatOverlayState
import site.addzero.kcloud.shell.navigation.RouteCatalog

@KoinApplication(
    modules = [
        UiScanModule::class,
        UiSupportModule::class,
    ],
)
object UiKoinApplication

@Module
@ComponentScan("site.addzero.kcloud")
class UiScanModule {
    @Single
    fun provideRouteCatalog(): RouteCatalog {
        return RouteCatalog(filterRouteMeta(RouteKeys.allMeta))
    }
}

@Module
class UiSupportModule {
    @Single
    fun provideAiAssistantOverlayState(): AddChatOverlayState {
        return AddChatOverlayState()
    }

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
