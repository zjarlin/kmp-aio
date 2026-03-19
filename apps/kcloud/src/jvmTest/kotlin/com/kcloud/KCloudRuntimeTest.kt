package com.kcloud

import com.kcloud.app.KCloudFeatureRegistry
import com.kcloud.feature.KCloudMenuGroups
import com.kcloud.feature.ShellSettingsService
import com.kcloud.features.ai.spi.AiDiagnosticsService
import com.kcloud.features.compose.ComposeFeatureMenus
import com.kcloud.features.dotfiles.DotfilesFeatureMenus
import com.kcloud.features.environment.EnvironmentFeatureMenus
import com.kcloud.features.file.FileFeatureMenus
import com.kcloud.features.notes.NotesFeatureMenus
import com.kcloud.features.packages.PackageOrganizerFeatureMenus
import com.kcloud.features.quicktransfer.QuickTransferDropService
import com.kcloud.features.quicktransfer.QuickTransferFeatureMenus
import com.kcloud.features.quicktransfer.QuickTransferService
import com.kcloud.features.servermanagement.ServerManagementFeatureMenus
import com.kcloud.features.ssh.SshFeatureMenus
import com.kcloud.features.settings.SettingsEditorService
import com.kcloud.features.settings.SettingsSectionRegistry
import com.kcloud.features.settings.SettingsFeatureMenus
import com.kcloud.features.transferhistory.TransferHistoryFeatureMenus
import com.kcloud.features.webdav.WebDavFeatureMenus
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.koin.core.context.stopKoin

class KCloudRuntimeTest {
    @Test
    fun `aggregates feature menus into sidebar tree`() {
        stopKoin()
        val runtime = createKCloudRuntime()

        try {
            val registry = runtime.koin.get<KCloudFeatureRegistry>()
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
                    QuickTransferFeatureMenus.QUICK_TRANSFER,
                    TransferHistoryFeatureMenus.TRANSFER_HISTORY
                ),
                rootNodes.getValue(KCloudMenuGroups.SYNC).children.map { node -> node.id }
            )
            assertEquals(
                listOf(
                    ServerManagementFeatureMenus.SERVER_MANAGEMENT,
                    ComposeFeatureMenus.COMPOSE_MANAGER,
                    FileFeatureMenus.FILE_MANAGER,
                    NotesFeatureMenus.NOTES,
                    PackageOrganizerFeatureMenus.PACKAGES,
                    SshFeatureMenus.SSH,
                    WebDavFeatureMenus.WEBDAV
                ),
                rootNodes.getValue(KCloudMenuGroups.MANAGEMENT).children.map { node -> node.id }
            )
            assertEquals(
                listOf(
                    DotfilesFeatureMenus.DOTFILES,
                    EnvironmentFeatureMenus.ENVIRONMENT_SETUP,
                    SettingsFeatureMenus.SETTINGS
                ),
                rootNodes.getValue(KCloudMenuGroups.SYSTEM).children.map { node -> node.id }
            )

            val visibleLeafIds = registry.visibleLeaves.map { node -> node.id }
            assertEquals(
                listOf(
                    QuickTransferFeatureMenus.QUICK_TRANSFER,
                    TransferHistoryFeatureMenus.TRANSFER_HISTORY,
                    ServerManagementFeatureMenus.SERVER_MANAGEMENT,
                    ComposeFeatureMenus.COMPOSE_MANAGER,
                    FileFeatureMenus.FILE_MANAGER,
                    NotesFeatureMenus.NOTES,
                    PackageOrganizerFeatureMenus.PACKAGES,
                    SshFeatureMenus.SSH,
                    WebDavFeatureMenus.WEBDAV,
                    DotfilesFeatureMenus.DOTFILES,
                    EnvironmentFeatureMenus.ENVIRONMENT_SETUP,
                    SettingsFeatureMenus.SETTINGS
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
    fun `registers initial ui and server features`() {
        stopKoin()
        val runtime = createKCloudRuntime()

        try {
            val featureRegistry = runtime.featureRegistry

            assertEquals(
                listOf(
                    "desktop-integration",
                    "quick-transfer",
                    "server-management",
                    "compose",
                    "file",
                    "notes",
                    "transfer-history",
                    "package-organizer",
                    "ssh",
                    "webdav",
                    "dotfiles",
                    "environment",
                    "settings"
                ),
                featureRegistry.features.map { feature -> feature.featureId }
            )
            assertEquals(
                listOf(
                    "quick-transfer",
                    "server-management",
                    "compose",
                    "file",
                    "notes",
                    "transfer-history",
                    "package-organizer",
                    "ssh",
                    "webdav",
                    "dotfiles",
                    "environment",
                    "ai"
                ),
                runtime.serverFeatures.map { feature -> feature.featureId }
            )
        } finally {
            runtime.stopServer()
            stopKoin()
        }
    }

    @Test
    fun `falls back to default leaf for unknown menu ids`() {
        stopKoin()
        val runtime = createKCloudRuntime()

        try {
            val registry = runtime.koin.get<KCloudFeatureRegistry>()

            assertEquals(registry.defaultLeafId, registry.normalizeMenuId(""))
            assertEquals(registry.defaultLeafId, registry.normalizeMenuId("quick"))
            assertEquals(registry.defaultLeafId, registry.normalizeMenuId("docker-compose"))
            assertEquals(NotesFeatureMenus.NOTES, registry.normalizeMenuId(NotesFeatureMenus.NOTES))
            assertNotNull(registry.findLeaf("quick")?.entry?.content)
            assertNotNull(registry.findLeaf(NotesFeatureMenus.NOTES)?.entry?.content)
        } finally {
            runtime.stopServer()
            stopKoin()
        }
    }

    @Test
    fun `resolves settings section registry`() {
        stopKoin()
        val runtime = createKCloudRuntime()

        try {
            val registry = runtime.koin.get<SettingsSectionRegistry>()

            assertTrue(registry.contributors.isNotEmpty())
            assertContains(
                registry.contributors.map { contributor -> contributor.sectionId },
                "settings.ai"
            )
        } finally {
            runtime.stopServer()
            stopKoin()
        }
    }

    @Test
    fun `registers ollama ai provider`() {
        stopKoin()
        val runtime = createKCloudRuntime()

        try {
            val diagnosticsService = runtime.koin.get<AiDiagnosticsService>()
            val providerIds = diagnosticsService.availableProviders().map { provider -> provider.providerId }

            assertContains(providerIds, "ollama")
        } finally {
            runtime.stopServer()
            stopKoin()
        }
    }

    @Test
    fun `resolves interface based shell and quick transfer services`() {
        stopKoin()
        val runtime = createKCloudRuntime()

        try {
            assertNotNull(runtime.koin.get<ShellSettingsService>())
            assertNotNull(runtime.koin.get<SettingsEditorService>())
            assertNotNull(runtime.koin.get<QuickTransferService>())
            assertNotNull(runtime.koin.get<QuickTransferDropService>())
        } finally {
            runtime.stopServer()
            stopKoin()
        }
    }
}
