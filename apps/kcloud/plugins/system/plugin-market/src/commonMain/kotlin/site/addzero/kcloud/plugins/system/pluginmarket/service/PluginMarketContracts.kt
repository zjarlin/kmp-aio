package site.addzero.kcloud.plugins.system.pluginmarket.service

import site.addzero.kcloud.plugins.system.pluginmarket.model.CreatePluginPackageRequest
import site.addzero.kcloud.plugins.system.pluginmarket.model.DeployPluginPackageRequest
import site.addzero.kcloud.plugins.system.pluginmarket.model.ImportDiscoveredPluginRequest
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginDeleteCheckResultDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginDeploymentJobDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginDiscoveryItemDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginMarketConfigDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginMarketSearchRequest
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginPackageAggregateDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginPackageDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginPresetKind
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginSourceFileDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.RunPluginBuildRequest
import site.addzero.kcloud.plugins.system.pluginmarket.model.SavePluginSourceFileRequest
import site.addzero.kcloud.plugins.system.pluginmarket.model.UpdatePluginMarketConfigRequest
import site.addzero.kcloud.plugins.system.pluginmarket.model.UpdatePluginPackageRequest

interface PluginPackageService {
    suspend fun create(request: CreatePluginPackageRequest): PluginPackageAggregateDto
    suspend fun list(search: PluginMarketSearchRequest = PluginMarketSearchRequest()): List<PluginPackageDto>
    suspend fun get(id: String): PluginPackageDto
    suspend fun aggregate(id: String): PluginPackageAggregateDto
    suspend fun update(id: String, request: UpdatePluginPackageRequest): PluginPackageDto
    suspend fun deleteCheck(id: String): PluginDeleteCheckResultDto
    suspend fun delete(id: String)
}

interface PluginSourceFileService {
    suspend fun list(packageId: String): List<PluginSourceFileDto>
    suspend fun save(request: SavePluginSourceFileRequest): PluginSourceFileDto
    suspend fun delete(fileId: String)
}

interface PluginPresetService {
    suspend fun applyPreset(packageId: String, presetKind: PluginPresetKind): PluginPackageAggregateDto
}

interface PluginDiscoveryService {
    suspend fun discover(search: PluginMarketSearchRequest = PluginMarketSearchRequest()): List<PluginDiscoveryItemDto>
    suspend fun importDiscovered(request: ImportDiscoveredPluginRequest): PluginPackageAggregateDto
}

interface PluginDeploymentService {
    suspend fun deploy(request: DeployPluginPackageRequest): PluginDeploymentJobDto
    suspend fun runBuild(request: RunPluginBuildRequest): PluginDeploymentJobDto
    suspend fun listJobs(packageId: String? = null): List<PluginDeploymentJobDto>
}

interface PluginMarketConfigService {
    suspend fun read(): PluginMarketConfigDto
    suspend fun update(request: UpdatePluginMarketConfigRequest): PluginMarketConfigDto
}
