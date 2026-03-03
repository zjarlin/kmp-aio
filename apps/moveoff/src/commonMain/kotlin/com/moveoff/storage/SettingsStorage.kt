package com.moveoff.storage

import com.moveoff.model.AppSettings
import com.moveoff.model.json
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.encodeToString
import java.io.File

private val logger = KotlinLogging.logger {}

expect fun getSettingsDirectory(): File

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
            logger.error(e) { "Failed to load settings" }
            AppSettings()
        }
    }

    fun saveSettings(settings: AppSettings) {
        try {
            val content = json.encodeToString(settings)
            settingsFile.writeText(content)
            logger.info { "Settings saved to ${settingsFile.absolutePath}" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to save settings" }
        }
    }
}
