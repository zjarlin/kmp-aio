package site.addzero.kcloud.plugins.system.pluginmarket.service

import site.addzero.kcloud.plugins.system.pluginmarket.model.*

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
