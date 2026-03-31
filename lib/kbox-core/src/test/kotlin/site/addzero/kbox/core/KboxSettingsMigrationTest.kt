package site.addzero.kbox.core

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import site.addzero.kbox.core.model.KboxSettings
import site.addzero.kbox.core.support.KboxDefaults

class KboxSettingsMigrationTest {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        coerceInputValues = true
    }

    @Test
    fun `old settings payload should receive sync defaults`() {
        val legacyPayload = """
            {
              "localAppDataOverride": "",
              "installerScanRoots": ["C:/Downloads"],
              "largeFileScanRoots": ["C:/Downloads"],
              "installerRules": [],
              "largeFileThresholdBytes": 1073741824,
              "ssh": {
                "enabled": true,
                "host": "example.com",
                "port": 22,
                "username": "demo",
                "authMode": "PASSWORD",
                "password": "secret",
                "privateKeyPath": "",
                "privateKeyPassphrase": "",
                "strictHostKeyChecking": false,
                "remotePath": {
                  "os": "LINUX",
                  "userHome": "",
                  "localAppData": "",
                  "appData": "",
                  "xdgDataHome": "",
                  "appName": "KBox"
                }
              }
            }
        """.trimIndent()

        val decoded = json.decodeFromString<KboxSettings>(legacyPayload)
        val normalized = KboxDefaults.normalize(decoded)

        assertEquals(false, normalized.syncEnabled)
        assertEquals(true, normalized.syncStartOnLaunch)
        assertEquals(30, normalized.syncRemotePollSeconds)
        assertTrue(normalized.syncMappings.isEmpty())
    }
}
