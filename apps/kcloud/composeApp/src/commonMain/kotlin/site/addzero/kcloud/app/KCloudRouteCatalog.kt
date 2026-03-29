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
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SmartToy
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
    val scenes: List<KCloudRouteScene> = buildScenes(
        routeMeta = routeMeta,
    )

    val routeEntries: List<KCloudRouteEntry> = scenes.flatMap { scene -> scene.routes }

    private val scenesById: Map<String, KCloudRouteScene> = scenes.associateBy { scene -> scene.id }
    private val routesByPath: Map<String, KCloudRouteEntry> = routeEntries.associateBy { route -> route.routePath }

    val defaultRoutePath: String = scenes.firstNotNullOfOrNull { scene ->
        scene.defaultRoutePath
    }.orEmpty()

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
        val scene = scenesById[sceneId] ?: return null
        return scene.defaultRoutePath ?: scene.routes.firstOrNull()?.routePath
    }

    fun breadcrumbNamesFor(
        routePath: String,
    ): List<String> {
        val route = routesByPath[routePath] ?: return emptyList()
        return buildList {
            add(route.sceneName)
            route.parentSegments.forEach { segment ->
                if (segment.isNotBlank() && segment != route.sceneName) {
                    add(segment)
                }
            }
            if (route.title.isNotBlank() && route.title != route.parentName) {
                add(route.title)
            }
        }
    }
}

data class KCloudRouteScene(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val sort: Int,
    val routes: List<KCloudRouteEntry>,
    val menuNodes: List<KCloudSidebarNode>,
    val defaultRoutePath: String?,
) {
    val routeCount: Int
        get() = routes.size
}

data class KCloudSidebarNode(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val children: List<KCloudSidebarNode> = emptyList(),
    val routePath: String? = null,
    val sort: Int = Int.MAX_VALUE,
) {
    val isLeaf: Boolean
        get() = routePath != null && children.isEmpty()
}

data class KCloudRouteEntry(
    val route: Route,
    val sceneId: String,
    val sceneName: String,
    val parentName: String,
    val parentSegments: List<String>,
    val icon: ImageVector,
    val sort: Int,
) {
    val routePath: String
        get() = route.routePath

    val title: String
        get() = route.title.ifBlank { route.simpleName.ifBlank { route.routePath } }
}

private fun buildScenes(
    routeMeta: List<Route>,
): List<KCloudRouteScene> {
    val orderedRoutes = routeMeta.sortedWith(
        compareBy<Route> { route -> route.order }
            .thenBy { route -> route.title }
            .thenBy { route -> route.routePath },
    )

    val sceneMap = linkedMapOf<String, MutableList<KCloudRouteEntry>>()
    val sceneMetaById = linkedMapOf<String, KCloudSceneMeta>()

    orderedRoutes.forEach { route ->
        val sceneMeta = route.resolveSceneMeta()
        val parentSegments = route.resolveMenuPath()
        val routeEntry = KCloudRouteEntry(
            route = route,
            sceneId = sceneMeta.id,
            sceneName = sceneMeta.name,
            parentName = parentSegments.lastOrNull().orEmpty(),
            parentSegments = parentSegments,
            icon = resolveKCloudIcon(route.icon),
            sort = route.order.toRouteSort(),
        )

        sceneMetaById.putIfAbsent(sceneMeta.id, sceneMeta)
        sceneMap.getOrPut(sceneMeta.id) { mutableListOf() }
            .add(routeEntry)
    }

    return sceneMap.mapNotNull { (sceneId, routes) ->
        val sceneMeta = sceneMetaById[sceneId] ?: return@mapNotNull null
        val sortedRoutes = routes.sortedWith(
            compareBy<KCloudRouteEntry> { route -> route.sort }
                .thenBy { route -> route.title }
                .thenBy { route -> route.routePath },
        )
        KCloudRouteScene(
            id = sceneMeta.id,
            name = sceneMeta.name,
            icon = resolveKCloudIcon(sceneMeta.iconName),
            sort = sceneMeta.sort,
            routes = sortedRoutes,
            menuNodes = buildSidebarNodes(
                sceneId = sceneMeta.id,
                routes = sortedRoutes,
            ),
            defaultRoutePath = sortedRoutes.firstOrNull { route ->
                route.route.placement.defaultInScene
            }?.routePath ?: sortedRoutes.firstOrNull()?.routePath,
        )
    }.sortedWith(
        compareBy<KCloudRouteScene> { scene -> scene.sort }
            .thenBy { scene -> scene.name },
    )
}

private fun buildSidebarNodes(
    sceneId: String,
    routes: List<KCloudRouteEntry>,
): List<KCloudSidebarNode> {
    val rootNodes = mutableListOf<MutableSidebarNode>()
    val branchIndex = linkedMapOf<String, MutableSidebarNode>()

    routes.forEach { route ->
        var currentChildren = rootNodes
        val parentTrail = mutableListOf<String>()

        route.parentSegments.forEach { segment ->
            parentTrail += segment
            val nodeId = "group/$sceneId/${parentTrail.joinToString("/")}"
            val branch = branchIndex.getOrPut(nodeId) {
                MutableSidebarNode(
                    id = nodeId,
                    name = segment,
                    icon = route.icon,
                    sort = route.sort,
                ).also { node ->
                    currentChildren += node
                }
            }
            branch.sort = minOf(branch.sort, route.sort)
            currentChildren = branch.children
        }

        currentChildren += MutableSidebarNode(
            id = route.routePath,
            name = route.title,
            icon = route.icon,
            sort = route.sort,
            routePath = route.routePath,
        )
    }

    return rootNodes.toImmutableNodes()
}

private fun MutableList<MutableSidebarNode>.toImmutableNodes(): List<KCloudSidebarNode> {
    return sortedWith(
        compareBy<MutableSidebarNode> { node -> node.sort }
            .thenBy { node -> node.name },
    ).map { node ->
        KCloudSidebarNode(
            id = node.id,
            name = node.name,
            icon = node.icon,
            children = node.children.toImmutableNodes(),
            routePath = node.routePath,
            sort = node.sort,
        )
    }
}

private data class MutableSidebarNode(
    val id: String,
    val name: String,
    val icon: ImageVector,
    var sort: Int,
    val routePath: String? = null,
    val children: MutableList<MutableSidebarNode> = mutableListOf(),
)

private data class KCloudSceneMeta(
    val id: String,
    val name: String,
    val iconName: String,
    val sort: Int,
)

private fun Route.resolveSceneMeta(): KCloudSceneMeta {
    val scene = placement.scene
    val sceneId = scene.id.trim()
    if (sceneId.isBlank()) {
        return KCloudSceneMeta(
            id = UNASSIGNED_SCENE_ID,
            name = UNASSIGNED_SCENE_NAME,
            iconName = "Apps",
            sort = Int.MAX_VALUE,
        )
    }
    return KCloudSceneMeta(
        id = sceneId,
        name = scene.name.trim().ifBlank { sceneId },
        iconName = scene.icon.ifBlank { "Apps" },
        sort = scene.order,
    )
}

private fun Route.resolveMenuPath(): List<String> {
    return placement.menuPath
        .map { segment -> segment.trim() }
        .filter { segment -> segment.isNotEmpty() }
}

internal fun resolveKCloudIcon(
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
        "MenuBook" -> Icons.Rounded.MenuBook
        "Person" -> Icons.Rounded.Person
        "PlayArrow" -> Icons.Rounded.PlayArrow
        "PowerSettingsNew" -> Icons.Rounded.PowerSettingsNew
        "Refresh" -> Icons.Rounded.Refresh
        "Search" -> Icons.Rounded.Search
        "Settings" -> Icons.Rounded.Settings
        "SmartToy" -> Icons.Rounded.SmartToy
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

private const val UNASSIGNED_SCENE_ID = "unassigned"
private const val UNASSIGNED_SCENE_NAME = "未分配场景"
