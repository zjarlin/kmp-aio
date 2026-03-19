package com.kcloud.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import com.kcloud.feature.KCloudMenuEntry
import com.kcloud.feature.KCloudMenuGroups
import com.kcloud.feature.KCloudMenuNode
import com.kcloud.feature.KCloudMenuTreeBuilder
import com.kcloud.feature.KCloudFeature
import com.kcloud.feature.ShellWindowController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single

@Single
class KCloudFeatureRegistry(
    features: List<KCloudFeature>
) {
    val features: List<KCloudFeature> = features.sortedBy { it.order }

    private val shellGroups = listOf(
        KCloudMenuEntry(
            id = KCloudMenuGroups.SYNC,
            title = "同步",
            icon = Icons.Default.Sync,
            sortOrder = 0
        ),
        KCloudMenuEntry(
            id = KCloudMenuGroups.MANAGEMENT,
            title = "管理",
            icon = Icons.Default.Folder,
            sortOrder = 1
        ),
        KCloudMenuEntry(
            id = KCloudMenuGroups.SYSTEM,
            title = "系统",
            icon = Icons.Default.Settings,
            sortOrder = 2
        )
    )

    val menuTree: List<KCloudMenuNode> = KCloudMenuTreeBuilder.buildTree(
        shellGroups + this.features.flatMap { feature -> feature.menuEntries }
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
        val normalized = menuId.trim()
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

@Single(binds = [ShellWindowController::class])
class KCloudShellState(
    private val featureRegistry: KCloudFeatureRegistry
) : ShellWindowController {
    private val _selectedMenuId = MutableStateFlow(featureRegistry.defaultLeafId)
    val selectedMenuId: StateFlow<String> = _selectedMenuId.asStateFlow()

    private val _expandedMenuIds = MutableStateFlow(featureRegistry.defaultExpandedIds)
    val expandedMenuIds: StateFlow<Set<String>> = _expandedMenuIds.asStateFlow()

    private val _windowVisible = MutableStateFlow(true)
    val windowVisible: StateFlow<Boolean> = _windowVisible.asStateFlow()

    private val _exitRequested = MutableStateFlow(false)
    val exitRequested: StateFlow<Boolean> = _exitRequested.asStateFlow()

    fun selectMenu(menuId: String) {
        val leaf = featureRegistry.findLeaf(menuId) ?: return
        _selectedMenuId.value = leaf.id
        expandAncestors(leaf.id)
    }

    fun toggleGroup(menuId: String) {
        val normalized = featureRegistry.normalizeMenuId(menuId)
        _expandedMenuIds.value = _expandedMenuIds.value.toMutableSet().apply {
            if (!add(normalized)) {
                remove(normalized)
            }
        }
    }

    private fun expandAncestors(menuId: String) {
        _expandedMenuIds.value = _expandedMenuIds.value + featureRegistry.ancestorIdsFor(menuId)
    }

    override fun showWindow() {
        _windowVisible.value = true
    }

    override fun hideWindow() {
        _windowVisible.value = false
    }

    override fun toggleWindow() {
        _windowVisible.value = !_windowVisible.value
    }

    override fun requestExit() {
        _exitRequested.value = true
    }
}
