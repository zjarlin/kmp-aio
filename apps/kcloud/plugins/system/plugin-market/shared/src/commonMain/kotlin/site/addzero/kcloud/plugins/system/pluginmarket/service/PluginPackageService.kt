package site.addzero.kcloud.plugins.system.pluginmarket.service

import site.addzero.kcloud.plugins.system.pluginmarket.model.CreatePluginPackageRequest
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginDeleteCheckResultDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginMarketSearchRequest
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginPackageAggregateDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginPackageDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.UpdatePluginPackageRequest

interface PluginPackageService {
    suspend fun create(request: CreatePluginPackageRequest): PluginPackageAggregateDto

    suspend fun list(search: PluginMarketSearchRequest = PluginMarketSearchRequest()): List<PluginPackageDto>

    suspend fun get(id: String): PluginPackageDto

    suspend fun aggregate(id: String): PluginPackageAggregateDto

    suspend fun update(id: String, request: UpdatePluginPackageRequest): PluginPackageDto

    suspend fun enable(id: String): PluginPackageDto

    suspend fun disable(id: String): PluginPackageDto

    suspend fun deleteCheck(id: String): PluginDeleteCheckResultDto

    suspend fun uninstall(id: String)

    suspend fun delete(id: String)
}
