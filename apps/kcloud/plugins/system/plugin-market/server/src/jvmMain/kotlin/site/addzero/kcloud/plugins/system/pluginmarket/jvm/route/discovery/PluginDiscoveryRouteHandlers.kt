package site.addzero.kcloud.plugins.system.pluginmarket.jvm.route.discovery

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import site.addzero.kcloud.plugins.system.pluginmarket.model.ImportDiscoveredPluginRequest
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginDiscoveryItemDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginMarketSearchRequest
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginPackageAggregateDto
import site.addzero.kcloud.plugins.system.pluginmarket.service.PluginDiscoveryService

/**
 * 磁盘插件发现列表。
 */
@GetMapping("/api/kcloud/plugin-market/packages/discover")
suspend fun discoverPluginPackages(
    @RequestParam("query") query: String?,
): List<PluginDiscoveryItemDto> {
    return discoveryService().discover(PluginMarketSearchRequest(query = query))
}

/**
 * 将发现模块导入为数据库托管插件。
 */
@PostMapping("/api/kcloud/plugin-market/packages/import")
suspend fun importDiscoveredPlugin(
    @RequestBody request: ImportDiscoveredPluginRequest,
): PluginPackageAggregateDto {
    return discoveryService().importDiscovered(request)
}

private fun discoveryService(): PluginDiscoveryService = KoinPlatform.getKoin().get()
