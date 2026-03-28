package site.addzero.kcloud.app

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.kcloud.app.render.KCloudContentRenderer
import site.addzero.kcloud.app.render.KCloudHeaderRenderer
import site.addzero.kcloud.app.render.KCloudSidebarRenderer
import site.addzero.kcloud.feature.ShellSettingsService
import site.addzero.workbenchshell.spi.content.WorkbenchContentRenderer
import site.addzero.workbenchshell.spi.header.WorkbenchHeaderRenderer
import site.addzero.workbenchshell.spi.sidebar.WorkbenchSidebarRenderer

@Module
@Configuration("kcloud-compose")
@ComponentScan("site.addzero.kcloud.app")
class KCloudWorkbenchKoinModule {
    @Single
    fun provideRouteCatalog(): KCloudRouteCatalog {
        return KCloudRouteCatalog(
            sceneDefinitions = KCloudSceneRegistry.all,
        )
    }

    @Single
    fun provideShellSettingsService(): ShellSettingsService {
        return DefaultShellSettingsService()
    }

    @Single
    fun provideShellState(
        routeCatalog: KCloudRouteCatalog,
    ): KCloudShellState {
        return KCloudShellState(
            routeCatalog = routeCatalog,
        )
    }

    @Single
    fun provideSidebarRenderer(
        routeCatalog: KCloudRouteCatalog,
        shellState: KCloudShellState,
    ): WorkbenchSidebarRenderer {
        return KCloudSidebarRenderer(
            routeCatalog = routeCatalog,
            shellState = shellState,
        )
    }

    @Single
    fun provideHeaderRenderer(
        routeCatalog: KCloudRouteCatalog,
        shellState: KCloudShellState,
    ): WorkbenchHeaderRenderer {
        return KCloudHeaderRenderer(
            routeCatalog = routeCatalog,
            shellState = shellState,
        )
    }

    @Single
    fun provideContentRenderer(
        routeCatalog: KCloudRouteCatalog,
        shellState: KCloudShellState,
    ): WorkbenchContentRenderer {
        return KCloudContentRenderer(
            routeCatalog = routeCatalog,
            shellState = shellState,
        )
    }
}
