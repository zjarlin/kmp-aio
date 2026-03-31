package site.addzero.kcloud.plugins.system.pluginmarket.service

import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginPackageAggregateDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginPresetKind

interface PluginPresetService {
    suspend fun applyPreset(packageId: String, presetKind: PluginPresetKind): PluginPackageAggregateDto
}
