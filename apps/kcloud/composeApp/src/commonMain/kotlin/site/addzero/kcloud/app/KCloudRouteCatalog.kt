package site.addzero.kcloud.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.math.roundToInt
import site.addzero.annotation.Route
import site.addzero.generated.RouteKeys

class KCloudRouteCatalog(
    routeMeta: List<Route> = RouteKeys.allMeta,
) {
    val scenes: List<KCloudRouteScene> = routeMeta
        .groupBy { route ->
            route.value.ifBlank { "未分组" }
        }
        .toList()
        .sortedBy { (_, routes) ->
            routes.minOfOrNull { route -> route.order } ?: Double.MAX_VALUE
        }
        .map { (sceneName, routes) ->
            val sceneId = "scene/$sceneName"
            val routeEntries = routes.sortedWith(
                compareBy<Route> { route -> route.order }
                    .thenBy { route -> route.title }
                    .thenBy { route -> route.routePath },
            ).map { route ->
                KCloudRouteEntry(
                    route = route,
                    sceneId = sceneId,
                    sceneName = sceneName,
                    icon = resolveRouteIcon(route.icon),
                    sort = route.order.toRouteSort(),
                )
            }
            KCloudRouteScene(
                id = sceneId,
                name = sceneName,
                icon = resolveRouteIcon(routes.firstOrNull()?.icon),
                sort = routes.minOfOrNull { route -> route.order.toRouteSort() } ?: Int.MAX_VALUE,
                routes = routeEntries,
            )
        }

    val routeEntries: List<KCloudRouteEntry> = scenes.flatMap { scene -> scene.routes }

    private val scenesById: Map<String, KCloudRouteScene> = scenes.associateBy { scene -> scene.id }
    private val routesByPath: Map<String, KCloudRouteEntry> = routeEntries.associateBy { route -> route.routePath }

    val defaultRoutePath: String = routeEntries.firstOrNull()?.routePath.orEmpty()

    fun findScene(
        sceneId: String,
    ): KCloudRouteScene? {
        return scenesById[sceneId]
    }

    fun findRoute(
        routePath: String,
    ): KCloudRouteEntry? {
        return routesByPath[routePath]
    }

    fun sceneIdFor(
        routePath: String,
    ): String {
        return routesByPath[routePath]?.sceneId ?: scenes.firstOrNull()?.id.orEmpty()
    }

    fun firstRoutePathUnder(
        sceneId: String,
    ): String? {
        return scenesById[sceneId]?.routes?.firstOrNull()?.routePath
    }

    fun breadcrumbNamesFor(
        routePath: String,
    ): List<String> {
        val route = routesByPath[routePath] ?: return emptyList()
        return listOf(route.sceneName, route.title)
    }
}

data class KCloudRouteScene(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val sort: Int,
    val routes: List<KCloudRouteEntry>,
) {
    val routeCount: Int
        get() = routes.size
}

data class KCloudRouteEntry(
    val route: Route,
    val sceneId: String,
    val sceneName: String,
    val icon: ImageVector,
    val sort: Int,
) {
    val routePath: String
        get() = route.routePath

    val title: String
        get() = route.title.ifBlank { route.simpleName.ifBlank { route.routePath } }
}

private fun resolveRouteIcon(
    iconName: String?,
): ImageVector {
    return when (iconName) {
        "AdminPanelSettings" -> Icons.Rounded.AdminPanelSettings
        "BugReport" -> Icons.Rounded.BugReport
        "Build" -> Icons.Rounded.Build
        "Dashboard" -> Icons.Rounded.Dashboard
        "Download" -> Icons.Rounded.Download
        "Help" -> Icons.AutoMirrored.Rounded.Help
        "Info" -> Icons.Rounded.Info
        "PlayArrow" -> Icons.Rounded.PlayArrow
        "PowerSettingsNew" -> Icons.Rounded.PowerSettingsNew
        "Refresh" -> Icons.Rounded.Refresh
        "Search" -> Icons.Rounded.Search
        "Settings" -> Icons.Rounded.Settings
        "Stop" -> Icons.Rounded.Stop
        "Sync" -> Icons.Rounded.Sync
        "Tune" -> Icons.Rounded.Tune
        "Upload" -> Icons.Rounded.Upload
        else -> Icons.Rounded.Apps
    }
}

private fun Double.toRouteSort(): Int {
    return (this * 1000).roundToInt()
}
