package site.addzero.remotecompose.server

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import site.addzero.remotecompose.shared.RemoteComposeCardNode
import site.addzero.remotecompose.shared.RemoteComposeColumnNode
import site.addzero.remotecompose.shared.RemoteComposeLocale

class RemoteComposeSchemaServiceTest {
    private val service = RemoteComposeSchemaService()

    @Test
    fun `list screens uses localized titles`() {
        val zhScreens = service.listScreens(RemoteComposeLocale.ZH_CN)
        val enScreens = service.listScreens(RemoteComposeLocale.EN_US)

        assertEquals(3, zhScreens.size)
        assertEquals("远程工作台总览", zhScreens.first().title)
        assertEquals("Remote Workbench Overview", enScreens.first().title)
    }

    @Test
    fun `load screen returns structured schema tree`() {
        val payload = service.loadScreen(
            screenId = "launchpad",
            locale = RemoteComposeLocale.ZH_CN,
        )

        assertEquals("launchpad", payload.screenId)
        val root = assertIs<RemoteComposeColumnNode>(payload.root)
        assertTrue(root.children.isNotEmpty())
        assertIs<RemoteComposeCardNode>(root.children.first())
    }
}
