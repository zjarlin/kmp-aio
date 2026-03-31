package site.addzero.kcloud.plugins.system.pluginmarket.service

import site.addzero.kcloud.plugins.system.pluginmarket.model.ImportDiscoveredPluginRequest
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginDiscoveryItemDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginMarketSearchRequest
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginPackageAggregateDto

interface PluginDiscoveryService {
    suspend fun discover(search: PluginMarketSearchRequest = PluginMarketSearchRequest()): List<PluginDiscoveryItemDto>

    suspend fun importDiscovered(request: ImportDiscoveredPluginRequest): PluginPackageAggregateDto
}
