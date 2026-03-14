package site.addzero.vibepocket.plugin

import com.kcloud.plugin.KCloudMenuEntry
import com.kcloud.plugin.KCloudMenuNode
import com.kcloud.plugin.KCloudMenuTreeBuilder
import com.kcloud.plugin.KCloudPlugin

class VibePocketPluginRegistry(
    plugins: List<KCloudPlugin> = vibePocketPlugins,
) {
    val plugins: List<KCloudPlugin> = plugins.sortedBy { it.order }

    private val shellGroups = listOf(
        KCloudMenuEntry(
            id = VibePocketMenuGroups.CREATE,
            title = "创作",
            sortOrder = 0,
        ),
        KCloudMenuEntry(
            id = VibePocketMenuGroups.TOOLS,
            title = "工具",
            sortOrder = 1,
        ),
        KCloudMenuEntry(
            id = VibePocketMenuGroups.SYSTEM,
            title = "系统",
            sortOrder = 2,
        ),
    )

    val menuTree: List<KCloudMenuNode> = KCloudMenuTreeBuilder.buildTree(
        shellGroups + this.plugins.flatMap { plugin -> plugin.menuEntries },
    )

    private val allNodes = KCloudMenuTreeBuilder.flatten(menuTree)
    private val nodesById = allNodes.associateBy { node -> node.id }

    val visibleLeaves: List<KCloudMenuNode> = KCloudMenuTreeBuilder.flattenVisibleLeaves(menuTree)
    val defaultLeafId: String = visibleLeaves.firstOrNull()?.id.orEmpty()
    val defaultExpandedIds: Set<String> = allNodes
        .filter { node -> node.children.isNotEmpty() }
        .map { node -> node.id }
        .toSet()

    private val legacyAliases = mapOf(
        "site.addzero.vibepocket.music.MusicVibeScreen" to VibePocketPluginMenus.MUSIC_STUDIO,
        "site.addzero.vibepocket.music.AudioToolsPage" to VibePocketPluginMenus.AUDIO_TOOLS,
        "site.addzero.vibepocket.settings.SettingsPage" to VibePocketPluginMenus.SETTINGS,
    )

    fun normalizeMenuId(menuId: String): String {
        val normalized = legacyAliases[menuId] ?: menuId
        return when {
            normalized.isBlank() -> defaultLeafId
            normalized in nodesById -> normalized
            else -> defaultLeafId
        }
    }

    fun findNode(menuId: String): KCloudMenuNode? {
        return nodesById[normalizeMenuId(menuId)]
    }

    fun findLeaf(menuId: String): KCloudMenuNode? {
        val normalized = normalizeMenuId(menuId)
        val node = nodesById[normalized]
        return when {
            node?.isLeaf == true -> node
            else -> visibleLeaves.firstOrNull()
        }
    }

    fun ancestorIdsFor(menuId: String): List<String> {
        return findNode(menuId)?.ancestorIds.orEmpty()
    }
}
