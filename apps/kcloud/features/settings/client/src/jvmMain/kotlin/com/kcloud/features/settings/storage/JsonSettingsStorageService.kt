package com.kcloud.features.settings.storage

import com.kcloud.model.AppSettings
import com.kcloud.feature.KCloudLocalPaths
import com.kcloud.feature.readKCloudJson
import com.kcloud.feature.writeKCloudJson
import com.kcloud.features.settings.SettingsStorageService
import java.io.File
import org.koin.core.annotation.Single

@Single(binds = [SettingsStorageService::class])
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
