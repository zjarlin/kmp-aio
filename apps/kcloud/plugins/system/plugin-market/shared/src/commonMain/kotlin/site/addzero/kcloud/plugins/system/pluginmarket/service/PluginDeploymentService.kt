package site.addzero.kcloud.plugins.system.pluginmarket.service

import site.addzero.kcloud.plugins.system.pluginmarket.model.DeployPluginPackageRequest
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginDeploymentJobDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.RunPluginBuildRequest

interface PluginDeploymentService {
    suspend fun deploy(request: DeployPluginPackageRequest): PluginDeploymentJobDto

    suspend fun runBuild(request: RunPluginBuildRequest): PluginDeploymentJobDto

    suspend fun listJobs(packageId: String? = null): List<PluginDeploymentJobDto>
}
