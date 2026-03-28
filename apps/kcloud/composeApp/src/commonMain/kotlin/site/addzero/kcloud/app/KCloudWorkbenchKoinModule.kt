package site.addzero.kcloud.app

import org.koin.dsl.module
import site.addzero.kcloud.app.render.KCloudContentRenderer
import site.addzero.kcloud.app.render.KCloudHeaderRenderer
import site.addzero.kcloud.app.render.KCloudSidebarRenderer
import site.addzero.kcloud.feature.ShellSettingsService
import site.addzero.workbenchshell.spi.content.WorkbenchContentRenderer
import site.addzero.workbenchshell.spi.header.WorkbenchHeaderRenderer
import site.addzero.workbenchshell.spi.sidebar.WorkbenchSidebarRenderer

val kCloudWorkbenchModule = module {
    single {
        KCloudRouteCatalog()
    }
    single<ShellSettingsService> {
        DefaultShellSettingsService()
    }
    single {
        KCloudShellState(
            routeCatalog = get(),
        )
    }
    single<WorkbenchSidebarRenderer> {
        KCloudSidebarRenderer(
            routeCatalog = get(),
            shellState = get(),
        )
    }
    single<WorkbenchHeaderRenderer> {
        KCloudHeaderRenderer(
            routeCatalog = get(),
            shellState = get(),
        )
    }
    single<WorkbenchContentRenderer> {
        KCloudContentRenderer(
            routeCatalog = get(),
            shellState = get(),
        )
    }
}
