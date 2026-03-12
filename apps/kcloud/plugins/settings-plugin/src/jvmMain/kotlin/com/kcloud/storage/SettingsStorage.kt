package com.kcloud.storage

import com.kcloud.model.AppSettings
import com.kcloud.model.json
import kotlinx.serialization.encodeToString
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

private val logger: Logger = Logger.getLogger("com.kcloud.storage.SettingsStorage")

object SettingsStorage {
    private val settingsFile by lazy {
        File(getSettingsDirectory(), "settings.json").also {
            it.parentFile?.mkdirs()
        }
    }

    fun loadSettings(): AppSettings {
        return try {
            if (settingsFile.exists()) {
                val content = settingsFile.readText()
                json.decodeFromString(content)
            } else {
                AppSettings()
            }
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Failed to load settings", e)
            AppSettings()
        }
    }

    fun saveSettings(settings: AppSettings) {
        try {
            val content = json.encodeToString(settings)
            settingsFile.writeText(content)
            logger.info("Settings saved to ${settingsFile.absolutePath}")
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Failed to save settings", e)
        }
    }
}
