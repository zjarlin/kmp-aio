package com.kcloud

import com.kcloud.plugin.KCloudMenuGroups
import com.kcloud.plugins.dotfiles.DotfilesPluginMenus
import com.kcloud.plugins.environment.EnvironmentPluginMenus
import com.kcloud.plugins.file.FilePluginMenus
import com.kcloud.plugins.notes.NotesPluginMenus
import com.kcloud.plugins.packages.PackageOrganizerPluginMenus
import com.kcloud.plugins.quicktransfer.QuickTransferPluginMenus
import com.kcloud.plugins.servermanagement.ServerManagementPluginMenus
import com.kcloud.plugins.ssh.SshPluginMenus
import com.kcloud.plugins.settings.SettingsPluginMenus
import com.kcloud.plugins.transferhistory.TransferHistoryPluginMenus
import com.kcloud.plugins.webdav.WebDavPluginMenus
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.koin.core.context.stopKoin

class KCloudRuntimeTest {
    @Test
    fun `aggregates plugin menus into sidebar tree`() {
        stopKoin()
        val runtime = createKCloudRuntime()

        try {
            val registry = runtime.koin.get<KCloudPluginRegistry>()
            val rootNodes = registry.menuTree.associateBy { node -> node.id }

            assertEquals(
                listOf(
                    KCloudMenuGroups.SYNC,
                    KCloudMenuGroups.MANAGEMENT,
                    KCloudMenuGroups.SYSTEM
                ),
                registry.menuTree.map { node -> node.id }
            )

            assertEquals(
                listOf(
                    QuickTransferPluginMenus.QUICK_TRANSFER,
                    TransferHistoryPluginMenus.TRANSFER_HISTORY
                ),
                rootNodes.getValue(KCloudMenuGroups.SYNC).children.map { node -> node.id }
            )
            assertEquals(
                listOf(
                    ServerManagementPluginMenus.SERVER_MANAGEMENT,
                    FilePluginMenus.FILE_MANAGER,
                    NotesPluginMenus.NOTES,
                    PackageOrganizerPluginMenus.PACKAGES,
                    SshPluginMenus.SSH,
                    WebDavPluginMenus.WEBDAV
                ),
                rootNodes.getValue(KCloudMenuGroups.MANAGEMENT).children.map { node -> node.id }
            )
            assertEquals(
                listOf(
                    DotfilesPluginMenus.DOTFILES,
                    EnvironmentPluginMenus.ENVIRONMENT_SETUP,
                    SettingsPluginMenus.SETTINGS
                ),
                rootNodes.getValue(KCloudMenuGroups.SYSTEM).children.map { node -> node.id }
            )

            val visibleLeafIds = registry.visibleLeaves.map { node -> node.id }
            assertEquals(
                listOf(
                    QuickTransferPluginMenus.QUICK_TRANSFER,
                    TransferHistoryPluginMenus.TRANSFER_HISTORY,
                    ServerManagementPluginMenus.SERVER_MANAGEMENT,
                    FilePluginMenus.FILE_MANAGER,
                    NotesPluginMenus.NOTES,
                    PackageOrganizerPluginMenus.PACKAGES,
                    SshPluginMenus.SSH,
                    WebDavPluginMenus.WEBDAV,
                    DotfilesPluginMenus.DOTFILES,
                    EnvironmentPluginMenus.ENVIRONMENT_SETUP,
                    SettingsPluginMenus.SETTINGS
                ),
                visibleLeafIds
            )
            registry.visibleLeaves.forEach { node ->
                assertEquals(1, node.level)
                assertEquals(listOf(node.parentId), node.ancestorIds)
            }
            assertTrue(registry.defaultLeafId.isNotBlank())
        } finally {
            runtime.stopServer()
            stopKoin()
        }
    }

    @Test
    fun `registers initial ui and server plugins`() {
        stopKoin()
        val runtime = createKCloudRuntime()

        try {
            val pluginRegistry = runtime.koin.get<KCloudPluginRegistry>()
            val serverPluginRegistry = runtime.koin.get<KCloudServerPluginRegistry>()

            assertEquals(
                listOf(
                    "desktop-integration-plugin",
                    "quick-transfer-plugin",
                    "server-management-plugin",
                    "file-plugin",
                    "notes-plugin",
                    "transfer-history-plugin",
                    "package-organizer-plugin",
                    "ssh-plugin",
                    "webdav-plugin",
                    "dotfiles-plugin",
                    "environment-plugin",
                    "settings-plugin"
                ),
                pluginRegistry.plugins.map { plugin -> plugin.pluginId }
            )
            assertEquals(
                listOf(
                    "quick-transfer-server-plugin",
                    "server-management-server-plugin",
                    "file-server-plugin",
                    "notes-server-plugin",
                    "transfer-history-server-plugin",
                    "package-organizer-server-plugin",
                    "ssh-server-plugin",
                    "webdav-server-plugin",
                    "dotfiles-server-plugin",
                    "environment-server-plugin"
                ),
                serverPluginRegistry.plugins.map { plugin -> plugin.pluginId }
            )
        } finally {
            runtime.stopServer()
            stopKoin()
        }
    }

    @Test
    fun `supports legacy menu aliases`() {
        stopKoin()
        val runtime = createKCloudRuntime()

        try {
            val registry = runtime.koin.get<KCloudPluginRegistry>()

            assertEquals(
                QuickTransferPluginMenus.QUICK_TRANSFER,
                registry.normalizeMenuId("quick")
            )
            assertEquals(
                FilePluginMenus.FILE_MANAGER,
                registry.normalizeMenuId("file")
            )
            assertEquals(NotesPluginMenus.NOTES, registry.normalizeMenuId("notes"))
            assertEquals(PackageOrganizerPluginMenus.PACKAGES, registry.normalizeMenuId("packages"))
            assertEquals(SshPluginMenus.SSH, registry.normalizeMenuId("ssh"))
            assertEquals(WebDavPluginMenus.WEBDAV, registry.normalizeMenuId("webdav"))
            assertEquals(DotfilesPluginMenus.DOTFILES, registry.normalizeMenuId("dotfiles"))
            assertEquals(EnvironmentPluginMenus.ENVIRONMENT_SETUP, registry.normalizeMenuId("environment"))
            assertNotNull(registry.findLeaf("quick")?.entry?.content)
            assertNotNull(registry.findLeaf("notes")?.entry?.content)
        } finally {
            runtime.stopServer()
            stopKoin()
        }
    }
}
