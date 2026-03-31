package site.addzero.kcloud.app

import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.core.network.ApiClientSpi
import site.addzero.core.network.ApiClients
import site.addzero.core.network.HttpClientFactory
import site.addzero.generated.RouteKeys
import site.addzero.kcloud.app.render.WorkbenchContentRender
import site.addzero.kcloud.app.render.WorkbenchHeaderRender
import site.addzero.kcloud.app.render.SidebarRenderImpl
import site.addzero.kcloud.feature.ShellSettingsService
import site.addzero.workbenchshell.spi.content.ContentRender
import site.addzero.workbenchshell.spi.header.HeaderRender
import site.addzero.workbenchshell.spi.sidebar.SidebarRender

@Module
class WorkbenchKoinModule {
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

    @Single
    fun provideRouteCatalog(): WorkbenchRouteCatalog {
        return WorkbenchRouteCatalog(RouteKeys.allMeta)
    }

    @Single
    fun provideShellSettingsService(): ShellSettingsService {
        return DefaultShellSettingsService()
    }

    @Single
    fun provideShellState(
        routeCatalog: WorkbenchRouteCatalog,
    ): WorkbenchShellState {
        return WorkbenchShellState(routeCatalog)
    }

    @Single
    fun provideSidebarRenderer(
        routeCatalog: WorkbenchRouteCatalog,
        shellState: WorkbenchShellState,
    ): SidebarRender {
        return SidebarRenderImpl(routeCatalog, shellState)
    }

    @Single
    fun provideHeaderRenderer(
        routeCatalog: WorkbenchRouteCatalog,
        shellState: WorkbenchShellState,
    ): HeaderRender {
        return WorkbenchHeaderRender(routeCatalog, shellState)
    }

    @Single
    fun provideContentRenderer(
        routeCatalog: WorkbenchRouteCatalog,
        shellState: WorkbenchShellState,
    ): ContentRender {
        return WorkbenchContentRender(routeCatalog, shellState)
    }
}
