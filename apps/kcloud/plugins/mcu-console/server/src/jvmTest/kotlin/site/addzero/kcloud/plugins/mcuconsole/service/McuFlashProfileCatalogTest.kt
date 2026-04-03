package site.addzero.kcloud.plugins.mcuconsole.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class McuFlashProfileCatalogTest {
    @Test
    fun `catalog exposes stm32 st-link profile`() {
        val response = McuFlashProfileCatalog().listProfiles()

        assertEquals("stm32-stlink-swd-f1-hd", response.defaultProfileId)
        assertEquals(1, response.items.size)
        assertEquals(listOf(0x414), response.items.single().supportedChipIds)
        assertTrue(response.items.single().connectUnderReset)
    }
}
