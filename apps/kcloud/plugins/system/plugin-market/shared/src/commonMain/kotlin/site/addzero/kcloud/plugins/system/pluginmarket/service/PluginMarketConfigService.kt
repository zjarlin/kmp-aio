package site.addzero.kcloud.plugins.system.pluginmarket.service

import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginMarketConfigDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.UpdatePluginMarketConfigRequest

interface PluginMarketConfigService {
    suspend fun read(): PluginMarketConfigDto

    suspend fun update(request: UpdatePluginMarketConfigRequest): PluginMarketConfigDto
}
