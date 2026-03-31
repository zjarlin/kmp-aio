package site.addzero.kbox.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Single
import site.addzero.annotation.Route
import site.addzero.generated.RouteKeys
import site.addzero.kbox.plugin.api.KboxDynamicRouteRegistry
import site.addzero.kbox.plugin.api.KboxRouteContribution
import kotlin.math.roundToInt

@Single
class KboxRouteCatalog(
    dynamicRouteRegistry: KboxDynamicRouteRegistry,
    private val staticRouteMeta: List<Route> = RouteKeys.allMeta,
) {
    val dynamicRoutes: StateFlow<List<KboxRouteContribution>> = dynamicRouteRegistry.dynamicRoutes

    val defaultRoutePath: String
        get() = scenes.firstNotNullOfOrNull { scene -> scene.defaultRoutePath }.orEmpty()

    val scenes: List<KboxRouteScene>
        get() = buildScenes(dynamicRoutes.value)

    val routeEntries: List<KboxRouteEntry>
        get() = scenes.flatMap { scene -> scene.routes }

    fun findScene(
        sceneId: String,
    ): KboxRouteScene? {
        return scenes.firstOrNull { scene -> scene.id == sceneId }
    }

    fun findRoute(
        routePath: String,
    ): KboxRouteEntry? {
        return routeEntries.firstOrNull { route -> route.routePath == routePath }
    }

    fun sceneIdFor(
        routePath: String,
    ): String {
        return findRoute(routePath)?.sceneId ?: scenes.firstOrNull()?.id.orEmpty()
    }

    fun firstRoutePathUnder(
        sceneId: String,
    ): String? {
        val scene = findScene(sceneId) ?: return null
        return scene.defaultRoutePath ?: scene.routes.firstOrNull()?.routePath
    }

    fun breadcrumbNamesFor(
        routePath: String,
    ): List<String> {
        val route = findRoute(routePath) ?: return emptyList()
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

    private fun buildScenes(
        runtimeRoutes: List<KboxRouteContribution>,
    ): List<KboxRouteScene> {
        val orderedRoutes = (staticRouteMeta.map { route -> route.toDefinition() } + runtimeRoutes.map { route -> route.toDefinition() })
            .sortedWith(
                compareBy<KboxRouteDefinition> { route -> route.order }
                    .thenBy { route -> route.title }
                    .thenBy { route -> route.routePath },
            )

        val sceneMap = linkedMapOf<String, MutableList<KboxRouteDefinition>>()
        val sceneMetaById = linkedMapOf<String, KboxSceneMeta>()

        orderedRoutes.forEach { route ->
            val sceneMeta = route.sceneMeta()
            sceneMetaById.putIfAbsent(sceneMeta.id, sceneMeta)
            sceneMap.getOrPut(sceneMeta.id) { mutableListOf() }
                .add(route)
        }

        return sceneMap.mapNotNull { (sceneId, routes) ->
            val sceneMeta = sceneMetaById[sceneId] ?: return@mapNotNull null
            val sortedRoutes = routes.map { route ->
                KboxRouteEntry(
                    routePath = route.routePath,
                    title = route.title,
                    sceneId = sceneMeta.id,
                    sceneName = sceneMeta.name,
                    parentName = route.parentName.trim(),
                    parentSegments = emptyList(),
                    icon = resolveKboxIcon(route.iconName),
                    sort = route.order.toRouteSort(),
                    runtimeContent = route.runtimeContent,
                    defaultInScene = route.defaultInScene,
                )
            }.sortedWith(
                compareBy<KboxRouteEntry> { route -> route.sort }
                    .thenBy { route -> route.title }
                    .thenBy { route -> route.routePath },
            )
            val finalizedRoutes = resolveRouteHierarchy(sortedRoutes)
            KboxRouteScene(
                id = sceneMeta.id,
                name = sceneMeta.name,
                icon = resolveKboxIcon(sceneMeta.iconName),
                sort = sceneMeta.sort,
                routes = finalizedRoutes,
                menuNodes = buildSidebarNodes(sceneMeta.id, finalizedRoutes),
                defaultRoutePath = finalizedRoutes.firstOrNull { route ->
                    route.defaultInScene
                }?.routePath ?: finalizedRoutes.firstOrNull()?.routePath,
            )
        }.sortedWith(
            compareBy<KboxRouteScene> { scene -> scene.sort }
                .thenBy { scene -> scene.name },
        )
    }
}

data class KboxRouteScene(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val sort: Int,
    val routes: List<KboxRouteEntry>,
    val menuNodes: List<KboxSidebarNode>,
    val defaultRoutePath: String?,
) {
    val routeCount: Int
        get() = routes.size
}

data class KboxSidebarNode(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val children: List<KboxSidebarNode> = emptyList(),
    val routePath: String? = null,
    val sort: Int = Int.MAX_VALUE,
) {
    val isLeaf: Boolean
        get() = routePath != null && children.isEmpty()
}

data class KboxRouteEntry(
    val routePath: String,
    val title: String,
    val sceneId: String,
    val sceneName: String,
    val parentName: String,
    val parentSegments: List<String>,
    val icon: ImageVector,
    val sort: Int,
    val runtimeContent: (@androidx.compose.runtime.Composable () -> Unit)? = null,
    val defaultInScene: Boolean = false,
)

private data class KboxRouteDefinition(
    val parentName: String,
    val title: String,
    val routePath: String,
    val iconName: String,
    val order: Double,
    val sceneName: String,
    val sceneIconName: String,
    val sceneOrder: Int,
    val defaultInScene: Boolean,
    val runtimeContent: (@androidx.compose.runtime.Composable () -> Unit)? = null,
)

private data class KboxSceneMeta(
    val id: String,
    val name: String,
    val iconName: String,
    val sort: Int,
)

private fun Route.toDefinition(): KboxRouteDefinition {
    val resolvedRoutePath = routePath.ifBlank {
        legacyKboxRoutePathFor(qualifiedName).orEmpty()
    }
    return KboxRouteDefinition(
        parentName = value,
        title = title.ifBlank { simpleName.ifBlank { routePath } },
        routePath = resolvedRoutePath,
        iconName = icon.ifBlank { "Apps" },
        order = order,
        sceneName = placement.scene.name,
        sceneIconName = placement.scene.icon.ifBlank { "Apps" },
        sceneOrder = placement.scene.order,
        defaultInScene = placement.defaultInScene,
    )
}

private fun KboxRouteContribution.toDefinition(): KboxRouteDefinition {
    return KboxRouteDefinition(
        parentName = parentName,
        title = title,
        routePath = routePath,
        iconName = iconName,
        order = order,
        sceneName = sceneName,
        sceneIconName = sceneIconName,
        sceneOrder = sceneOrder,
        defaultInScene = defaultInScene,
        runtimeContent = content,
    )
}

private fun legacyKboxRoutePathFor(
    qualifiedName: String,
): String? {
    return when (qualifiedName) {
        KBOX_PLUGIN_MANAGER_QUALIFIED_NAME -> KBOX_PLUGIN_MANAGER_ROUTE_PATH
        KBOX_STORAGE_TOOL_QUALIFIED_NAME -> KBOX_STORAGE_TOOL_ROUTE_PATH
        else -> null
    }
}

private fun KboxRouteDefinition.sceneMeta(): KboxSceneMeta {
    val normalizedName = sceneName.trim()
    if (normalizedName.isBlank()) {
        return KboxSceneMeta(
            id = UNASSIGNED_SCENE_ID,
            name = UNASSIGNED_SCENE_NAME,
            iconName = "Apps",
            sort = Int.MAX_VALUE,
        )
    }
    return KboxSceneMeta(
        id = normalizedName,
        name = normalizedName,
        iconName = sceneIconName.ifBlank { "Apps" },
        sort = sceneOrder,
    )
}

private fun resolveRouteHierarchy(
    routes: List<KboxRouteEntry>,
): List<KboxRouteEntry> {
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
        route.copy(parentSegments = resolveParentSegments(route.parentName))
    }
}

private fun buildSidebarNodes(
    sceneId: String,
    routes: List<KboxRouteEntry>,
): List<KboxSidebarNode> {
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
                    icon = route.icon,
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
        routeNode.icon = route.icon
        routeNode.routePath = route.routePath
    }

    return rootNodes.toImmutableNodes()
}

private fun MutableList<MutableSidebarNode>.toImmutableNodes(): List<KboxSidebarNode> {
    return sortedWith(
        compareBy<MutableSidebarNode> { node -> node.sort }
            .thenBy { node -> node.name },
    ).map { node ->
        KboxSidebarNode(
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

internal fun resolveKboxIcon(
    iconName: String?,
): ImageVector {
    return when (iconName) {
        "AdminPanelSettings" -> Icons.Rounded.AdminPanelSettings
        "CloudUpload" -> Icons.Rounded.CloudUpload
        "Extension" -> Icons.Rounded.Extension
        "FolderZip" -> Icons.Rounded.FolderZip
        "Help" -> Icons.AutoMirrored.Rounded.Help
        "Inventory2" -> Icons.Rounded.Inventory2
        "Settings" -> Icons.Rounded.Settings
        "Storage" -> Icons.Rounded.Storage
        "Tune" -> Icons.Rounded.Tune
        else -> Icons.Rounded.Apps
    }
}

private fun Double.toRouteSort(): Int {
    return (this * 1000).roundToInt()
}

private const val UNASSIGNED_SCENE_ID = "unassigned"
private const val KBOX_PLUGIN_MANAGER_QUALIFIED_NAME = "site.addzero.kbox.plugins.system.pluginmanager.screen.KboxPluginManagerScreen"
private const val KBOX_PLUGIN_MANAGER_ROUTE_PATH = "system/plugin-manager"
private const val KBOX_STORAGE_TOOL_QUALIFIED_NAME = "site.addzero.kbox.plugins.tools.storagetool.screen.KboxStorageToolScreen"
private const val KBOX_STORAGE_TOOL_ROUTE_PATH = "tools/storage-tool"
private const val UNASSIGNED_SCENE_NAME = "未分配场景"
