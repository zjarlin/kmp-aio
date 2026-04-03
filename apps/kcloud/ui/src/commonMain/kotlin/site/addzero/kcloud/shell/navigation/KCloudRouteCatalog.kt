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
import site.addzero.annotation.Route
import site.addzero.compose.icons.IconMap
import kotlin.math.roundToInt

class KCloudRouteCatalog(
    val routeMeta: List<Route>,
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
    val sidebarIcon: ImageVector,
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

    val sceneMap = linkedMapOf<String, MutableList<Route>>()
    val sceneMetaById = linkedMapOf<String, KCloudSceneMeta>()

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
            KCloudRouteEntry(
                route = route,
                sceneId = sceneMeta.id,
                sceneName = sceneMeta.name,
                parentName = route.resolveParentName(),
                parentSegments = emptyList(),
                icon = resolveKCloudIcon(route.icon),
                sidebarIcon = resolveKCloudSidebarLeafIcon(
                    iconName = route.icon,
                    routeTitle = route.title.ifBlank { route.simpleName.ifBlank { route.routePath } },
                    parentName = route.resolveParentName(),
                ),
                sort = route.order.toRouteSort(),
            )
        }.sortedWith(
            compareBy<KCloudRouteEntry> { route -> route.sort }
                .thenBy { route -> route.title }
                .thenBy { route -> route.routePath },
        )
        val finalizedRoutes = resolveRouteHierarchy(sortedRoutes)
        KCloudRouteScene(
            id = sceneMeta.id,
            name = sceneMeta.name,
            icon = resolveKCloudIcon(sceneMeta.iconName),
            sort = sceneMeta.sort,
            routes = finalizedRoutes,
            menuNodes = buildSidebarNodes(
                sceneId = sceneMeta.id,
                routes = finalizedRoutes,
            ),
            defaultRoutePath = finalizedRoutes.firstOrNull { route ->
                route.route.placement.defaultInScene
            }?.routePath ?: finalizedRoutes.firstOrNull()?.routePath,
        )
    }.sortedWith(
        compareBy<KCloudRouteScene> { scene -> scene.sort }
            .thenBy { scene -> scene.name },
    )
}

private fun resolveRouteHierarchy(
    routes: List<KCloudRouteEntry>,
): List<KCloudRouteEntry> {
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
    routes: List<KCloudRouteEntry>,
): List<KCloudSidebarNode> {
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
                    icon = resolveKCloudSidebarGroupIcon(name),
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
    var icon: ImageVector,
    var sort: Int,
    var routePath: String? = null,
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
    val sceneName = normalizeSceneName(scene.name.trim())
    if (sceneName.isBlank()) {
        return KCloudSceneMeta(
            id = UNASSIGNED_SCENE_ID,
            name = UNASSIGNED_SCENE_NAME,
            iconName = "Apps",
            sort = Int.MAX_VALUE,
        )
    }
    return KCloudSceneMeta(
        id = sceneName,
        name = sceneName,
        iconName = scene.icon.ifBlank { "Apps" },
        sort = scene.order,
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

private fun Route.resolveParentName(): String {
    return value.trim()
}

internal fun resolveKCloudIcon(
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

private fun resolveKCloudSidebarGroupIcon(
    groupName: String,
): ImageVector {
    return when {
        groupName.contains("设备") || groupName.contains("会话") -> Icons.Outlined.Terminal
        groupName.contains("开发") || groupName.contains("工具") -> Icons.Outlined.Construction
        groupName.contains("配置") -> Icons.Outlined.SettingsApplications
        else -> Icons.Outlined.Folder
    }
}

private fun resolveKCloudSidebarLeafIcon(
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
            else -> resolveKCloudIcon(iconName)
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
