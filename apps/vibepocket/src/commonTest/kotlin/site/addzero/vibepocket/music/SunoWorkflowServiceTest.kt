package site.addzero.vibepocket.music

import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SunoWorkflowServiceTest {
    @AfterTest
    fun tearDown() {
        SunoWorkflowService.resetForTests()
    }

    @Test
    fun loadConfigFallsBackToDefaultsWhenConfigReadFails() = runTest {
        SunoWorkflowService.configLoader = {
            error("config backend unavailable")
        }

        assertEquals(SunoRuntimeConfig(), SunoWorkflowService.loadConfig())
    }

    @Test
    fun getCreditsReturnsNullWhenTokenMissing() = runTest {
        var clientCreated = false
        SunoWorkflowService.configLoader = { SunoRuntimeConfig(apiToken = "") }
        SunoWorkflowService.clientFactory = {
            clientCreated = true
            error("client should not be created when token is blank")
        }

        assertEquals(null, SunoWorkflowService.getCreditsOrNull())
        assertFalse(clientCreated)
    }
}
