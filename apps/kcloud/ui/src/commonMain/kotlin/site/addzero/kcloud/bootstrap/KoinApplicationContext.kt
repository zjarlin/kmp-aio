package site.addzero.kcloud.bootstrap

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.core.network.HttpClientFactory
import site.addzero.generated.RouteKeys
import site.addzero.component.chat.AddChatOverlayState
import site.addzero.kcloud.shell.navigation.RouteCatalog


@KoinApplication
object KoinApplication

@Module
@ComponentScan("site.addzero")
class KoinModule {
    @Single
    fun provideRouteCatalog(): RouteCatalog {
        return RouteCatalog(RouteKeys.allMeta)
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
}
