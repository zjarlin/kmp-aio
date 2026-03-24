package site.addzero.kcloud

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.koin.core.context.stopKoin
import site.addzero.kcloud.feature.KCloudScenePlugin
import site.addzero.kcloud.feature.KCloudScreenRoots
import site.addzero.kcloud.scenes.notes.NotesSceneMenus
import site.addzero.kcloud.scenes.ops.OpsSceneMenus
import site.addzero.kcloud.scenes.secondbrain.SecondBrainSceneMenus
import site.addzero.kcloud.scenes.system.SystemSceneMenus
import site.addzero.kcloud.scenes.workspace.WorkspaceSceneMenus

class KCloudRuntimeTest {
    @Test
    fun `aggregates five scene plugins into the runtime catalog`() {
        stopKoin()
        val runtime = createKCloudRuntime()

        try {
            val scenePlugins = runtime.koin.getAll<KCloudScenePlugin>().sortedBy { plugin -> plugin.sort }
            val rootNodes = runtime.screenCatalog.tree.associateBy { node -> node.id }

            assertEquals(
                listOf(
                    KCloudScreenRoots.WORKSPACE,
                    KCloudScreenRoots.NOTES,
                    KCloudScreenRoots.SECOND_BRAIN,
                    KCloudScreenRoots.OPS,
                    KCloudScreenRoots.SYSTEM,
                ),
                scenePlugins.map { plugin -> plugin.sceneId },
            )
            assertEquals(
                listOf(
                    KCloudScreenRoots.WORKSPACE,
                    KCloudScreenRoots.NOTES,
                    KCloudScreenRoots.SECOND_BRAIN,
                    KCloudScreenRoots.OPS,
                    KCloudScreenRoots.SYSTEM,
                ),
                runtime.screenCatalog.tree.map { node -> node.id },
            )
            assertEquals(
                listOf(WorkspaceSceneMenus.OVERVIEW, WorkspaceSceneMenus.SYNC_CENTER),
                rootNodes.getValue(KCloudScreenRoots.WORKSPACE).children.map { node -> node.id },
            )
            assertEquals(
                listOf(NotesSceneMenus.OVERVIEW, NotesSceneMenus.COLLECTIONS),
                rootNodes.getValue(KCloudScreenRoots.NOTES).children.map { node -> node.id },
            )
            assertEquals(
                listOf(SecondBrainSceneMenus.ASSETS, SecondBrainSceneMenus.ARCHIVE),
                rootNodes.getValue(KCloudScreenRoots.SECOND_BRAIN).children.map { node -> node.id },
            )
            assertEquals(
                listOf(OpsSceneMenus.SERVERS, OpsSceneMenus.SETTINGS),
                rootNodes.getValue(KCloudScreenRoots.OPS).children.map { node -> node.id },
            )
            assertEquals(
                listOf(SystemSceneMenus.RBAC, SystemSceneMenus.ENVIRONMENT),
                rootNodes.getValue(KCloudScreenRoots.SYSTEM).children.map { node -> node.id },
            )
        } finally {
            runtime.stopServer()
            stopKoin()
        }
    }

    @Test
    fun `scene plugins expose summary pages for HTTP aggregation`() {
        stopKoin()
        val runtime = createKCloudRuntime()

        try {
            val scenePlugins = runtime.koin.getAll<KCloudScenePlugin>().associateBy { plugin -> plugin.sceneId }

            assertEquals(
                listOf(WorkspaceSceneMenus.OVERVIEW, WorkspaceSceneMenus.SYNC_CENTER),
                scenePlugins.getValue(KCloudScreenRoots.WORKSPACE).pages.map { page -> page.pageId },
            )
            assertEquals(
                listOf(SystemSceneMenus.RBAC, SystemSceneMenus.ENVIRONMENT),
                scenePlugins.getValue(KCloudScreenRoots.SYSTEM).pages.map { page -> page.pageId },
            )
        } finally {
            runtime.stopServer()
            stopKoin()
        }
    }

    @Test
    fun `selecting a scene switches to that scene's first visible leaf`() {
        stopKoin()
        val runtime = createKCloudRuntime()

        try {
            runtime.shellState.selectScene(KCloudScreenRoots.SYSTEM)

            assertEquals(KCloudScreenRoots.SYSTEM, runtime.shellState.selectedSceneId.value)
            assertEquals(SystemSceneMenus.RBAC, runtime.shellState.selectedScreenId.value)

            runtime.shellState.selectScene(KCloudScreenRoots.WORKSPACE)

            assertEquals(KCloudScreenRoots.WORKSPACE, runtime.shellState.selectedSceneId.value)
            assertEquals(WorkspaceSceneMenus.OVERVIEW, runtime.shellState.selectedScreenId.value)
            assertTrue(runtime.screenCatalog.defaultLeafId.isNotBlank())
        } finally {
            runtime.stopServer()
            stopKoin()
        }
    }
}
