package site.addzero.kbox.core.service

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import site.addzero.kbox.core.model.KboxSettings
import site.addzero.kbox.core.support.KboxDefaults

@Single
class KboxSettingsRepository(
    private val json: Json,
    private val pathService: KboxPathService,
) {
    fun load(): KboxSettings {
        val settingsFile = pathService.settingsFile()
        if (!settingsFile.isFile) {
            return save(KboxDefaults.defaultSettings())
        }
        val decoded = json.decodeFromString<KboxSettings>(settingsFile.readText())
        return KboxDefaults.normalize(decoded)
    }

    fun save(
        settings: KboxSettings,
    ): KboxSettings {
        val normalized = KboxDefaults.normalize(settings)
        val settingsFile = pathService.settingsFile()
        settingsFile.parentFile?.mkdirs()
        settingsFile.writeText(json.encodeToString(normalized))
        return normalized
    }
}
