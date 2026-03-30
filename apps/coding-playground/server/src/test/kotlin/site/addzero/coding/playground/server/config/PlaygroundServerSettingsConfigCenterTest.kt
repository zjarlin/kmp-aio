package site.addzero.coding.playground.server.config

import java.nio.file.Files
import java.sql.DriverManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlaygroundServerSettingsConfigCenterTest {
    @Test
    fun settingsAndHttpToggleCanBeLoadedFromConfigCenter() {
        val tempDir = Files.createTempDirectory("playground-config-center-test")
        val dbPath = tempDir.resolve("playground-config.db")
        val dbUrl = "jdbc:sqlite:$dbPath"
        Class.forName("org.sqlite.JDBC")
        DriverManager.getConnection(dbUrl).use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate(
                    """
                    CREATE TABLE config_center_value (
                        id TEXT PRIMARY KEY,
                        namespace TEXT NOT NULL,
                        active_profile TEXT NOT NULL,
                        config_key TEXT NOT NULL,
                        config_value TEXT NOT NULL,
                        create_time TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        update_time TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """.trimIndent(),
                )
                statement.executeUpdate(
                    """
                    INSERT INTO config_center_value(id, namespace, active_profile, config_key, config_value)
                    VALUES
                        ('1', 'coding-playground', 'dev', 'coding.playground.server.port', '19090'),
                        ('2', 'coding-playground', 'dev', 'coding.playground.http.enabled', 'true')
                    """.trimIndent(),
                )
            }
        }

        val previousDbUrl = System.getProperty("coding.playground.db.url")
        val previousDataDir = System.getProperty("coding.playground.data.dir")
        val previousActive = System.getProperty("coding.playground.active")
        try {
            System.setProperty("coding.playground.db.url", dbUrl)
            System.setProperty("coding.playground.data.dir", tempDir.toString())
            System.setProperty("coding.playground.active", "dev")

            val settings = PlaygroundServerSettings.fromSystem()

            assertEquals(19090, settings.serverPort)
            assertTrue(defaultPlaygroundHttpServerEnabled())
        } finally {
            restoreSystemProperty("coding.playground.db.url", previousDbUrl)
            restoreSystemProperty("coding.playground.data.dir", previousDataDir)
            restoreSystemProperty("coding.playground.active", previousActive)
        }
    }
}

private fun restoreSystemProperty(
    key: String,
    value: String?,
) {
    if (value == null) {
        System.clearProperty(key)
    } else {
        System.setProperty(key, value)
    }
}
