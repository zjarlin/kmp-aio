package site.addzero.kcloud.app

import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.core.network.HttpClientFactory
import site.addzero.kcloud.app.render.KCloudContentRender
import site.addzero.kcloud.app.render.KCloudHeaderRender
import site.addzero.kcloud.app.render.SidebarRenderImpl
import site.addzero.kcloud.feature.ShellSettingsService
import site.addzero.workbenchshell.spi.content.ContentRender
import site.addzero.workbenchshell.spi.header.HeaderRender
import site.addzero.workbenchshell.spi.sidebar.SidebarRenderer

@Module
class KCloudWorkbenchKoinModule {
    @Single
    // Compose root explicitly owns this boundary bean because the external default module
    // is not reliably aggregated into the desktop Koin application in the current setup.
    fun provideHttpClientFactory(): HttpClientFactory {
        return HttpClientFactory()
    }

    @Single
    fun provideShellSettingsService(): ShellSettingsService {
        return DefaultShellSettingsService()
    }

    @Single
    fun provideRouteCatalog(): KCloudRouteCatalog {
        return KCloudRouteCatalog()
    }

    @Single
    fun provideShellState(
        routeCatalog: KCloudRouteCatalog,
    ): KCloudShellState {
        return KCloudShellState(routeCatalog)
    }

    @Single
    fun provideSidebarRenderer(
        routeCatalog: KCloudRouteCatalog,
        shellState: KCloudShellState,
    ): SidebarRenderer {
        return SidebarRenderImpl(
            routeCatalog = routeCatalog,
            shellState = shellState,
        )
    }

    @Single
    fun provideHeaderRenderer(
        routeCatalog: KCloudRouteCatalog,
        shellState: KCloudShellState,
    ): HeaderRender {
        return KCloudHeaderRender(
            routeCatalog = routeCatalog,
            shellState = shellState,
        )
    }

    @Single
    fun provideContentRenderer(
        routeCatalog: KCloudRouteCatalog,
        shellState: KCloudShellState,
    ): ContentRender {
        return KCloudContentRender(
            routeCatalog = routeCatalog,
            shellState = shellState,
        )
    }
}
