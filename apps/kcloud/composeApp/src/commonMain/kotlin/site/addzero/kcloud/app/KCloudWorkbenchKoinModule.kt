package site.addzero.kcloud.app

import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.generated.RouteKeys
import site.addzero.kcloud.app.render.KCloudContentRender
import site.addzero.kcloud.app.render.KCloudHeaderRender
import site.addzero.kcloud.app.render.SidebarRenderImpl
import site.addzero.kcloud.feature.ShellSettingsService
import site.addzero.workbenchshell.spi.content.ContentRender
import site.addzero.workbenchshell.spi.header.HeaderRender
import site.addzero.workbenchshell.spi.sidebar.SidebarRender

@Module
class KCloudWorkbenchKoinModule {
    @Single
    fun provideRouteCatalog(): KCloudRouteCatalog {
        return KCloudRouteCatalog(RouteKeys.allMeta)
    }

    @Single
    fun provideShellSettingsService(): ShellSettingsService {
        return DefaultShellSettingsService()
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
    ): SidebarRender {
        return SidebarRenderImpl(routeCatalog, shellState)
    }

    @Single
    fun provideHeaderRenderer(
        routeCatalog: KCloudRouteCatalog,
        shellState: KCloudShellState,
    ): HeaderRender {
        return KCloudHeaderRender(routeCatalog, shellState)
    }

    @Single
    fun provideContentRenderer(
        routeCatalog: KCloudRouteCatalog,
        shellState: KCloudShellState,
    ): ContentRender {
        return KCloudContentRender(routeCatalog, shellState)
    }
}
