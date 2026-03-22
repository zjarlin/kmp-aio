package site.addzero.vibepocket.render

import org.koin.core.context.GlobalContext
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import site.addzero.vibepocket.feature.vibePocketScreens
import site.addzero.vibepocket.screens.creativeassets.CreativeAssetsViewModel
import site.addzero.vibepocket.screens.musicstudio.MusicStudioViewModel
import site.addzero.vibepocket.screens.settings.SettingsViewModel
import site.addzero.workbenchshell.Screen
import site.addzero.workbenchshell.ScreenCatalog
import site.addzero.workbenchshell.spi.content.WorkbenchContentRenderer
import site.addzero.workbenchshell.spi.header.WorkbenchHeaderRenderer
import site.addzero.workbenchshell.spi.sidebar.WorkbenchSidebarRenderer

fun createVibePocketWorkbenchModule(): Module = module {
    vibePocketScreens.forEach { screen ->
        single<Screen>(qualifier = named(screen.id)) {
            screen
        }
    }

    single {
        ScreenCatalog(
            screens = getKoin().getAll(),
        )
    }
    single {
        VibePocketShellState(
            screenCatalog = get(),
        )
    }
    single {
        MusicStudioViewModel()
    }
    single {
        CreativeAssetsViewModel()
    }
    single {
        SettingsViewModel()
    }
    single<WorkbenchSidebarRenderer> {
        VibePocketSidebarRenderer(
            screenCatalog = get(),
            shellState = get(),
        )
    }
    single<WorkbenchHeaderRenderer> {
        VibePocketHeaderRenderer(
            screenCatalog = get(),
            shellState = get(),
        )
    }
    single<WorkbenchContentRenderer> {
        VibePocketContentRenderer(
            screenCatalog = get(),
            shellState = get(),
        )
    }
}

/** 桌面宿主在 server 启动后把工作台壳层定义补进同一套全局 Koin。 */
fun installVibePocketWorkbenchModule(
    module: Module = createVibePocketWorkbenchModule(),
): Module {
    GlobalContext.get().loadModules(
        modules = listOf(module),
        createEagerInstances = false,
    )
    return module
}

/** 进程退出前卸载宿主壳层定义，避免同进程重复启动残留旧定义。 */
fun uninstallVibePocketWorkbenchModule(
    module: Module,
) {
    GlobalContext.get().unloadModules(
        modules = listOf(module),
    )
}
