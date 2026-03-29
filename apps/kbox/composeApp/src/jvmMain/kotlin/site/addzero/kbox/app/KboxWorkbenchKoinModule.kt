package site.addzero.kbox.app

import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.kbox.app.render.KboxContentRender
import site.addzero.kbox.app.render.KboxHeaderRender
import site.addzero.kbox.app.render.KboxSidebarRenderer
import site.addzero.kbox.feature.KboxShellSettingsService
import site.addzero.kbox.plugin.api.KboxDynamicRouteRegistry
import site.addzero.workbenchshell.spi.content.ContentRender
import site.addzero.workbenchshell.spi.header.HeaderRender
import site.addzero.workbenchshell.spi.sidebar.SidebarRender

@Module
class KboxWorkbenchKoinModule {
    @Single
    fun provideRouteCatalog(
        dynamicRouteRegistry: KboxDynamicRouteRegistry,
    ): KboxRouteCatalog {
        return KboxRouteCatalog(dynamicRouteRegistry)
    }

    @Single
    fun provideShellSettingsService(): KboxShellSettingsService {
        return DefaultKboxShellSettingsService()
    }

    @Single
    fun provideShellState(
        routeCatalog: KboxRouteCatalog,
    ): KboxShellState {
        return KboxShellState(routeCatalog)
    }

    @Single
    fun provideSidebarRenderer(
        routeCatalog: KboxRouteCatalog,
        shellState: KboxShellState,
    ): SidebarRender {
        return KboxSidebarRenderer(routeCatalog, shellState)
    }

    @Single
    fun provideHeaderRenderer(
        routeCatalog: KboxRouteCatalog,
        shellState: KboxShellState,
    ): HeaderRender {
        return KboxHeaderRender(routeCatalog, shellState)
    }

    @Single
    fun provideContentRenderer(
        routeCatalog: KboxRouteCatalog,
        shellState: KboxShellState,
    ): ContentRender {
        return KboxContentRender(routeCatalog, shellState)
    }
}
