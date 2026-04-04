package site.addzero.kcloud.shell.navigation

import org.koin.core.annotation.Single
import site.addzero.annotation.Route
import site.addzero.generated.RouteKeys
import site.addzero.workbenchshell.spi.screen.BasicScreen
import site.addzero.workbenchshell.spi.screen.Screen
import site.addzero.workbenchshell.spi.screen.ScreenSource

@Single
class GeneratedRouteScreenSource : ScreenSource {
    override val priority: Int = 100

    override fun listScreens(): List<Screen> {
        return RouteKeys.allMeta.map(Route::toScreen)
    }
}

private fun Route.toScreen(): Screen {
    return BasicScreen(
        value = value,
        title = title,
        routePath = routePath,
        icon = icon,
        order = order,
        enabled = enabled,
        sceneName = placement.scene.name,
        sceneIcon = placement.scene.icon,
        sceneOrder = placement.scene.order,
        defaultInScene = placement.defaultInScene,
        qualifiedName = qualifiedName,
        simpleName = simpleName,
    )
}
