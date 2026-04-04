package site.addzero.kcloud.shell.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.SettingsApplications
import androidx.compose.material.icons.outlined.SettingsEthernet
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.ui.graphics.vector.ImageVector
import org.koin.core.annotation.Single
import site.addzero.compose.icons.IconMap
import site.addzero.workbenchshell.spi.screen.Screen
import kotlin.math.roundToInt

@Single
class RouteCatalog(
    screenRegistry: ScreenRegistry,
) {
    val routeMeta: List<Screen> = screenRegistry.screens
    val scenes = buildScenes(
        routeMeta = routeMeta,
    )
    val routeEntries = scenes.flatMap { scene -> scene.routes }
    private val scenesById = scenes.associateBy { scene -> scene.id }
    private val routesByPath = routeEntries.associateBy { route -> route.routePath }

    val defaultRoutePath = scenes.firstNotNullOfOrNull { scene ->
        scene.defaultRoutePath
    }.orEmpty()

    fun findScene(
        sceneId: String,
    ): RouteScene? {
        return scenesById[sceneId]
    }

    fun findRoute(
        routePath: String,
    ): RouteEntry? {
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

data class RouteScene(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val sort: Int,
    val routes: List<RouteEntry>,
    val menuNodes: List<SidebarNode>,
    val defaultRoutePath: String?,
) {
    val routeCount: Int
        get() = routes.size
}

data class SidebarNode(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val children: List<SidebarNode> = emptyList(),
    val routePath: String? = null,
    val sort: Int = Int.MAX_VALUE,
) {
    val isLeaf: Boolean
        get() = routePath != null && children.isEmpty()
}

data class RouteEntry(
    val route: Screen,
    val sceneId: String,
    val sceneName: String,
    val parentName: String,
    val parentSegments: List<String>,
    val icon: ImageVector,
    val sidebarIcon: ImageVector,
    val sort: Int,
) {
    val routePath: String
        get() = route.routePath

    val title: String
        get() = route.title.ifBlank { route.simpleName.ifBlank { route.routePath } }
}

private fun buildScenes(
    routeMeta: List<Screen>,
): List<RouteScene> {
    val orderedRoutes = routeMeta.sortedWith(
        compareBy<Screen> { route -> route.order }
            .thenBy { route -> route.title }
            .thenBy { route -> route.routePath },
    )

    val sceneMap = linkedMapOf<String, MutableList<Screen>>()
    val sceneMetaById = linkedMapOf<String, SceneMeta>()

    orderedRoutes.forEach { route ->
        val sceneMeta = route.resolveSceneMeta()
        if (sceneMeta.id !in sceneMetaById) {
            sceneMetaById[sceneMeta.id] = sceneMeta
        }
        sceneMap.getOrPut(sceneMeta.id) { mutableListOf() }
            .add(route)
    }

    return sceneMap.mapNotNull { (sceneId, routes) ->
        val sceneMeta = sceneMetaById[sceneId] ?: return@mapNotNull null
        val sortedRoutes = routes.map { route ->
            RouteEntry(
                route = route,
                sceneId = sceneMeta.id,
                sceneName = sceneMeta.name,
                parentName = route.resolveParentName(),
                parentSegments = emptyList(),
                icon = resolveRouteIcon(route.icon),
                sidebarIcon = resolveSidebarLeafIcon(
                    iconName = route.icon,
                    routeTitle = route.title.ifBlank { route.simpleName.ifBlank { route.routePath } },
                    parentName = route.resolveParentName(),
                ),
                sort = route.order.toRouteSort(),
            )
        }.sortedWith(
            compareBy<RouteEntry> { route -> route.sort }
                .thenBy { route -> route.title }
                .thenBy { route -> route.routePath },
        )
        val finalizedRoutes = resolveRouteHierarchy(sortedRoutes)
        RouteScene(
            id = sceneMeta.id,
            name = sceneMeta.name,
            icon = resolveRouteIcon(sceneMeta.iconName),
            sort = sceneMeta.sort,
            routes = finalizedRoutes,
            menuNodes = buildSidebarNodes(
                sceneId = sceneMeta.id,
                routes = finalizedRoutes,
            ),
            defaultRoutePath = finalizedRoutes.firstOrNull { route ->
                route.route.defaultInScene
            }?.routePath ?: finalizedRoutes.firstOrNull()?.routePath,
        )
    }.sortedWith(
        compareBy<RouteScene> { scene -> scene.sort }
            .thenBy { scene -> scene.name },
    )
}

private fun resolveRouteHierarchy(
    routes: List<RouteEntry>,
): List<RouteEntry> {
    val parentByTitle = linkedMapOf<String, String>()
    routes.forEach { route ->
        val title = route.title.trim()
        val parentName = route.parentName.trim()
        if (title.isNotBlank() && parentName.isNotBlank() && title !in parentByTitle) {
            parentByTitle[title] = parentName
        }
    }

    val segmentCache = mutableMapOf<String, List<String>>()
    val visiting = linkedSetOf<String>()

    fun resolveParentSegments(parentName: String): List<String> {
        val normalizedParent = parentName.trim()
        if (normalizedParent.isBlank()) {
            return emptyList()
        }
        segmentCache[normalizedParent]?.let { cached ->
            return cached
        }

        if (!visiting.add(normalizedParent)) {
            return listOf(normalizedParent)
        }

        return try {
            val explicitSegments = normalizedParent.split('/')
                .map { segment -> segment.trim() }
                .filter { segment -> segment.isNotEmpty() }
            val resolvedSegments = if (explicitSegments.size > 1) {
                explicitSegments
            } else {
                val upperParent = parentByTitle[normalizedParent].orEmpty()
                val ancestorSegments = if (upperParent.isBlank() || upperParent == normalizedParent) {
                    emptyList()
                } else {
                    resolveParentSegments(upperParent)
                }
                ancestorSegments + normalizedParent
            }
            segmentCache[normalizedParent] = resolvedSegments
            resolvedSegments
        } finally {
            visiting.remove(normalizedParent)
        }
    }

    return routes.map { route ->
        route.copy(
            parentSegments = resolveParentSegments(route.parentName),
        )
    }
}

private fun buildSidebarNodes(
    sceneId: String,
    routes: List<RouteEntry>,
): List<SidebarNode> {
    val rootNodes = mutableListOf<MutableSidebarNode>()
    val nodeIndex = linkedMapOf<String, MutableSidebarNode>()

    routes.forEach { route ->
        var currentChildren = rootNodes
        val nodeTrail = mutableListOf<String>()

        fun findOrCreateNode(name: String): MutableSidebarNode {
            nodeTrail += name
            val nodeId = "group/$sceneId/${nodeTrail.joinToString("/")}"
            return nodeIndex.getOrPut(nodeId) {
                MutableSidebarNode(
                    id = nodeId,
                    name = name,
                    icon = resolveSidebarGroupIcon(name),
                    sort = route.sort,
                ).also { node ->
                    currentChildren += node
                }
            }
        }

        route.parentSegments.forEach { segment ->
            val parentNode = findOrCreateNode(segment)
            parentNode.sort = minOf(parentNode.sort, route.sort)
            currentChildren = parentNode.children
        }

        val routeNode = findOrCreateNode(route.title)
        routeNode.sort = minOf(routeNode.sort, route.sort)
        routeNode.icon = route.sidebarIcon
        routeNode.routePath = route.routePath
    }

    return rootNodes.toImmutableNodes()
}

private fun MutableList<MutableSidebarNode>.toImmutableNodes(): List<SidebarNode> {
    return sortedWith(
        compareBy<MutableSidebarNode> { node -> node.sort }
            .thenBy { node -> node.name },
    ).map { node ->
        SidebarNode(
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
    var icon: ImageVector,
    var sort: Int,
    var routePath: String? = null,
    val children: MutableList<MutableSidebarNode> = mutableListOf(),
)

private data class SceneMeta(
    val id: String,
    val name: String,
    val iconName: String,
    val sort: Int,
)

private fun Screen.resolveSceneMeta(): SceneMeta {
    val sceneName = normalizeSceneName(sceneName.trim())
    if (sceneName.isBlank()) {
        return SceneMeta(
            id = UNASSIGNED_SCENE_ID,
            name = UNASSIGNED_SCENE_NAME,
            iconName = "Apps",
            sort = Int.MAX_VALUE,
        )
    }
    return SceneMeta(
        id = sceneName,
        name = sceneName,
        iconName = sceneIcon.ifBlank { "Apps" },
        sort = sceneOrder,
    )
}

private fun normalizeSceneName(
    sceneName: String,
): String {
    return when (sceneName) {
        "物联网上位机" -> "物联网"
        else -> sceneName
    }
}

private fun Screen.resolveParentName(): String {
    return value.trim()
}

internal fun resolveRouteIcon(
    iconName: String?,
): ImageVector {
    val normalizedIconName = iconName?.trim()
        .takeUnless { it.isNullOrEmpty() }
        ?: DEFAULT_KCLOUD_ICON_NAME
    return KCLOUD_ICON_TYPE_PRIORITY.firstNotNullOfOrNull { iconType ->
        IconMap.allIcons.firstOrNull { icon ->
            icon.iconKey == normalizedIconName && icon.iconType == iconType
        }?.vector
    } ?: IconMap.allIcons.firstOrNull { icon ->
        icon.iconKey == DEFAULT_KCLOUD_ICON_NAME && icon.iconType == DEFAULT_KCLOUD_ICON_TYPE
    }?.vector ?: error("compose-icon-map 缺少默认图标 $DEFAULT_KCLOUD_ICON_NAME")
}

private fun resolveSidebarGroupIcon(
    groupName: String,
): ImageVector {
    return when {
        groupName.contains("设备") || groupName.contains("会话") -> Icons.Outlined.Terminal
        groupName.contains("开发") || groupName.contains("工具") -> Icons.Outlined.Construction
        groupName.contains("配置") -> Icons.Outlined.SettingsApplications
        else -> Icons.Outlined.Folder
    }
}

private fun resolveSidebarLeafIcon(
    iconName: String?,
    routeTitle: String,
    parentName: String,
): ImageVector {
    return when (iconName?.trim()) {
        "PowerSettingsNew" -> Icons.Outlined.PowerSettingsNew
        "Upload" -> Icons.Outlined.Upload
        "SettingsInputComponent" -> Icons.Outlined.SettingsEthernet
        "Code" -> Icons.Outlined.Code
        "BugReport" -> Icons.Outlined.BugReport
        "Key" -> Icons.Outlined.Key
        else -> when {
            routeTitle.contains("配置") -> Icons.Outlined.Key
            routeTitle.contains("调试") -> Icons.Outlined.BugReport
            routeTitle.contains("开发") -> Icons.Outlined.Code
            parentName.contains("设备") || parentName.contains("会话") -> Icons.Outlined.Terminal
            else -> resolveRouteIcon(iconName)
        }
    }
}

private fun Double.toRouteSort(): Int {
    return (this * 1000).roundToInt()
}

private const val UNASSIGNED_SCENE_ID = "unassigned"
private const val UNASSIGNED_SCENE_NAME = "未分配场景"
private const val DEFAULT_KCLOUD_ICON_NAME = "Apps"
private const val DEFAULT_KCLOUD_ICON_TYPE = "Filled"
private val KCLOUD_ICON_TYPE_PRIORITY = listOf(
    "Filled",
    "AutoMirroredFilled",
    "Outlined",
    "AutoMirroredOutlined",
)
