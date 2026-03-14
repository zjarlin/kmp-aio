package com.kcloud.plugins.settings

import com.kcloud.model.AppSettings
import com.kcloud.plugin.KCloudLocalPaths
import com.kcloud.plugin.readKCloudJson
import com.kcloud.plugin.writeKCloudJson
import java.io.File
import org.koin.core.annotation.Single

@Single
class JsonSettingsStorageService : SettingsStorageService {
    private val settingsFile: File by lazy {
        File(KCloudLocalPaths.appSupportDir(), "settings.json")
    }

    override fun loadSettings(): AppSettings {
        return readKCloudJson(settingsFile) { AppSettings() }
    }

    override fun saveSettings(settings: AppSettings) {
        writeKCloudJson(settingsFile, settings)
    }
}
