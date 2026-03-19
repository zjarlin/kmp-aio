package site.addzero.vibepocket.feature

import com.kcloud.feature.KCloudFeature
import com.kcloud.feature.KCloudMenuEntry
import com.kcloud.feature.KCloudMenuNode
import com.kcloud.feature.KCloudMenuTreeBuilder

class VibePocketFeatureRegistry(
    features: List<KCloudFeature> = vibePocketFeatures,
) {
    val features: List<KCloudFeature> = features.sortedBy { it.order }

    private val shellGroups = listOf(
        KCloudMenuEntry(
            id = VibePocketMenuGroups.CREATE,
            title = "创作",
            sortOrder = 0,
        ),
        KCloudMenuEntry(
            id = VibePocketMenuGroups.SYSTEM,
            title = "系统",
            sortOrder = 1,
        ),
    )

    val menuTree: List<KCloudMenuNode> = KCloudMenuTreeBuilder.buildTree(
        shellGroups + this.features.flatMap { feature -> feature.menuEntries },
    )

    private val allNodes = KCloudMenuTreeBuilder.flatten(menuTree)
    private val nodesById = allNodes.associateBy { node -> node.id }

    val visibleLeaves: List<KCloudMenuNode> = KCloudMenuTreeBuilder.flattenVisibleLeaves(menuTree)
    val defaultLeafId: String = visibleLeaves.firstOrNull()?.id.orEmpty()
    val defaultExpandedIds: Set<String> = allNodes
        .filter { node -> node.children.isNotEmpty() }
        .map { node -> node.id }
        .toSet()

    fun normalizeMenuId(menuId: String): String {
        return when {
            menuId.isBlank() -> defaultLeafId
            menuId in nodesById -> menuId
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
