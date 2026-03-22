package site.addzero.kcloud

import site.addzero.kcloud.feature.DesktopLifecycleContributor
import site.addzero.kcloud.feature.KCloudScreenRoots
import site.addzero.kcloud.feature.ShellSettingsService
import site.addzero.kcloud.features.ai.spi.AiDiagnosticsService
import site.addzero.kcloud.features.compose.ComposeFeatureMenus
import site.addzero.kcloud.features.dotfiles.DotfilesFeatureMenus
import site.addzero.kcloud.features.environment.EnvironmentFeatureMenus
import site.addzero.kcloud.features.file.FileFeatureMenus
import site.addzero.kcloud.features.notes.NotesFeatureMenus
import site.addzero.kcloud.features.packages.PackageOrganizerFeatureMenus
import site.addzero.kcloud.features.quicktransfer.QuickTransferDropService
import site.addzero.kcloud.features.quicktransfer.QuickTransferFeatureMenus
import site.addzero.kcloud.features.quicktransfer.QuickTransferService
import site.addzero.kcloud.features.rbac.RbacFeatureMenus
import site.addzero.kcloud.features.servermanagement.ServerManagementFeatureMenus
import site.addzero.kcloud.features.ssh.SshFeatureMenus
import site.addzero.kcloud.plugins.settings.SettingsEditorService
import site.addzero.kcloud.plugins.settings.SettingsSectionRegistry
import site.addzero.kcloud.plugins.settings.SettingsFeatureMenus
import site.addzero.kcloud.features.transferhistory.TransferHistoryFeatureMenus
import site.addzero.kcloud.features.webdav.WebDavFeatureMenus
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.koin.core.context.stopKoin
import site.addzero.workbenchshell.Screen
import site.addzero.workbenchshell.ScreenCatalog

class KCloudRuntimeTest {
    @Test
    fun `odiajsodi`() {
        val getenv = System.getenv("LOCALAPPDATA")
        println()

    }
    @Test
    fun `aggregates screens into sidebar tree`() {
        stopKoin()
        val runtime = createKCloudRuntime()

        try {
            val catalog = runtime.koin.get<ScreenCatalog>()
            val rootNodes = catalog.tree.associateBy { node -> node.id }

            assertEquals(
                listOf(
                    KCloudScreenRoots.WORKSPACE,
                    KCloudScreenRoots.NOTES,
                    KCloudScreenRoots.SECOND_BRAIN,
                    KCloudScreenRoots.OPS,
                    KCloudScreenRoots.SYSTEM,
                ),
                catalog.tree.map { node -> node.id },
            )

            assertEquals(
                listOf(
                    QuickTransferFeatureMenus.QUICK_TRANSFER,
                    FileFeatureMenus.FILE_MANAGER,
                    TransferHistoryFeatureMenus.TRANSFER_HISTORY,
                    WebDavFeatureMenus.WEBDAV,
                ),
                rootNodes.getValue(KCloudScreenRoots.WORKSPACE).children.map { node -> node.id },
            )
            assertEquals(
                listOf(
                    NotesFeatureMenus.NOTES,
                ),
                rootNodes.getValue(KCloudScreenRoots.NOTES).children.map { node -> node.id },
            )
            assertEquals(
                listOf(
                    PackageOrganizerFeatureMenus.PACKAGES,
                    DotfilesFeatureMenus.DOTFILES,
                ),
                rootNodes.getValue(KCloudScreenRoots.SECOND_BRAIN).children.map { node -> node.id },
            )
            assertEquals(
                listOf(
                    ServerManagementFeatureMenus.SERVER_MANAGEMENT,
                    ComposeFeatureMenus.COMPOSE_MANAGER,
                    SshFeatureMenus.SSH,
                    _root_ide_package_.site.addzero.kcloud.plugins.settings.SettingsFeatureMenus.SETTINGS,
                ),
                rootNodes.getValue(KCloudScreenRoots.OPS).children.map { node -> node.id },
            )
            assertEquals(
                listOf(
                    RbacFeatureMenus.RBAC,
                    EnvironmentFeatureMenus.ENVIRONMENT_SETUP,
                ),
                rootNodes.getValue(KCloudScreenRoots.SYSTEM).children.map { node -> node.id },
            )

            val visibleLeafIds = catalog.visibleLeafNodes.map { node -> node.id }
            assertEquals(
                listOf(
                    QuickTransferFeatureMenus.QUICK_TRANSFER,
                    FileFeatureMenus.FILE_MANAGER,
                    TransferHistoryFeatureMenus.TRANSFER_HISTORY,
                    WebDavFeatureMenus.WEBDAV,
                    NotesFeatureMenus.NOTES,
                    PackageOrganizerFeatureMenus.PACKAGES,
                    DotfilesFeatureMenus.DOTFILES,
                    ServerManagementFeatureMenus.SERVER_MANAGEMENT,
                    ComposeFeatureMenus.COMPOSE_MANAGER,
                    SshFeatureMenus.SSH,
                    _root_ide_package_.site.addzero.kcloud.plugins.settings.SettingsFeatureMenus.SETTINGS,
                    RbacFeatureMenus.RBAC,
                    EnvironmentFeatureMenus.ENVIRONMENT_SETUP,
                ),
                visibleLeafIds,
            )
            catalog.visibleLeafNodes.forEach { node ->
                assertEquals(1, node.level)
                assertEquals(listOf(node.pid), node.ancestorIds)
            }
            assertTrue(catalog.defaultLeafId.isNotBlank())
        } finally {
            runtime.stopServer()
            stopKoin()
        }
    }

    @Test
    fun `resolves screen lifecycle and server collections`() {
        stopKoin()
        val runtime = createKCloudRuntime()

        try {
            val screens = runtime.koin.getAll<Screen>()
            val lifecycleContributors = runtime.koin.getAll<DesktopLifecycleContributor>()

            assertEquals(
                listOf(
                    QuickTransferFeatureMenus.QUICK_TRANSFER,
                    FileFeatureMenus.FILE_MANAGER,
                    TransferHistoryFeatureMenus.TRANSFER_HISTORY,
                    WebDavFeatureMenus.WEBDAV,
                    NotesFeatureMenus.NOTES,
                    PackageOrganizerFeatureMenus.PACKAGES,
                    DotfilesFeatureMenus.DOTFILES,
                    ServerManagementFeatureMenus.SERVER_MANAGEMENT,
                    ComposeFeatureMenus.COMPOSE_MANAGER,
                    SshFeatureMenus.SSH,
                    _root_ide_package_.site.addzero.kcloud.plugins.settings.SettingsFeatureMenus.SETTINGS,
                    RbacFeatureMenus.RBAC,
                    EnvironmentFeatureMenus.ENVIRONMENT_SETUP,
                ).sorted(),
                screens.map { screen -> screen.id }.sorted(),
            )
            assertTrue(lifecycleContributors.isNotEmpty())
            assertTrue(runtime.serverLifecycleContributors.isNotEmpty())
        } finally {
            runtime.stopServer()
            stopKoin()
        }
    }

    @Test
    fun `falls back to default leaf for unknown screen ids`() {
        stopKoin()
        val runtime = createKCloudRuntime()

        try {
            val catalog = runtime.koin.get<ScreenCatalog>()

            assertEquals(catalog.defaultLeafId, catalog.normalizeScreenId(""))
            assertEquals(catalog.defaultLeafId, catalog.normalizeScreenId("quick"))
            assertEquals(catalog.defaultLeafId, catalog.normalizeScreenId("docker-compose"))
            assertEquals(NotesFeatureMenus.NOTES, catalog.normalizeScreenId(NotesFeatureMenus.NOTES))
            assertNotNull(catalog.findLeaf("quick")?.content)
            assertNotNull(catalog.findLeaf(NotesFeatureMenus.NOTES)?.content)
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
            val registry = runtime.koin.get<site.addzero.kcloud.plugins.settings.SettingsSectionRegistry>()

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
            assertNotNull(runtime.koin.get<site.addzero.kcloud.plugins.settings.SettingsEditorService>())
            assertNotNull(runtime.koin.get<QuickTransferService>())
            assertNotNull(runtime.koin.get<QuickTransferDropService>())
        } finally {
            runtime.stopServer()
            stopKoin()
        }
    }
}
