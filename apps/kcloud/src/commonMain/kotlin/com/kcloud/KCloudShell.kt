package com.kcloud

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import com.kcloud.plugin.KCloudMenuEntry
import com.kcloud.plugin.KCloudMenuGroups
import com.kcloud.plugin.KCloudMenuNode
import com.kcloud.plugin.KCloudMenuTreeBuilder
import com.kcloud.plugin.KCloudPlugin
import com.kcloud.plugin.ShellWindowController
import com.kcloud.plugins.dotfiles.DotfilesPluginMenus
import com.kcloud.plugins.environment.EnvironmentPluginMenus
import com.kcloud.plugins.file.FilePluginMenus
import com.kcloud.plugins.notes.NotesPluginMenus
import com.kcloud.plugins.packages.PackageOrganizerPluginMenus
import com.kcloud.plugins.quicktransfer.QuickTransferPluginMenus
import com.kcloud.plugins.servermanagement.ServerManagementPluginMenus
import com.kcloud.plugins.settings.SettingsPluginMenus
import com.kcloud.plugins.ssh.SshPluginMenus
import com.kcloud.plugins.transferhistory.TransferHistoryPluginMenus
import com.kcloud.plugins.webdav.WebDavPluginMenus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single

@Single
class KCloudPluginRegistry(
    plugins: List<KCloudPlugin>
) {
    val plugins: List<KCloudPlugin> = plugins.sortedBy { it.order }

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
        shellGroups + this.plugins.flatMap { plugin -> plugin.menuEntries }
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
        "quick" to QuickTransferPluginMenus.QUICK_TRANSFER,
        "file" to FilePluginMenus.FILE_MANAGER,
        "notes" to NotesPluginMenus.NOTES,
        "packages" to PackageOrganizerPluginMenus.PACKAGES,
        "server" to ServerManagementPluginMenus.SERVER_MANAGEMENT,
        "ssh" to SshPluginMenus.SSH,
        "history" to TransferHistoryPluginMenus.TRANSFER_HISTORY,
        "webdav" to WebDavPluginMenus.WEBDAV,
        "dotfiles" to DotfilesPluginMenus.DOTFILES,
        "environment" to EnvironmentPluginMenus.ENVIRONMENT_SETUP,
        "settings" to SettingsPluginMenus.SETTINGS
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

@Single
class KCloudShellState(
    private val pluginRegistry: KCloudPluginRegistry
) : ShellWindowController {
    private val _selectedMenuId = MutableStateFlow(pluginRegistry.defaultLeafId)
    val selectedMenuId: StateFlow<String> = _selectedMenuId.asStateFlow()

    private val _expandedMenuIds = MutableStateFlow(pluginRegistry.defaultExpandedIds)
    val expandedMenuIds: StateFlow<Set<String>> = _expandedMenuIds.asStateFlow()

    private val _windowVisible = MutableStateFlow(true)
    val windowVisible: StateFlow<Boolean> = _windowVisible.asStateFlow()

    private val _exitRequested = MutableStateFlow(false)
    val exitRequested: StateFlow<Boolean> = _exitRequested.asStateFlow()

    fun selectMenu(menuId: String) {
        val leaf = pluginRegistry.findLeaf(menuId) ?: return
        _selectedMenuId.value = leaf.id
        expandAncestors(leaf.id)
    }

    fun toggleGroup(menuId: String) {
        val normalized = pluginRegistry.normalizeMenuId(menuId)
        _expandedMenuIds.value = _expandedMenuIds.value.toMutableSet().apply {
            if (!add(normalized)) {
                remove(normalized)
            }
        }
    }

    private fun expandAncestors(menuId: String) {
        _expandedMenuIds.value = _expandedMenuIds.value + pluginRegistry.ancestorIdsFor(menuId)
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
