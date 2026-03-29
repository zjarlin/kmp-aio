package site.addzero.kcloud.plugins.system.pluginmarket.jvm.route.deploy

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginDeploymentJobDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.DeployPluginPackageRequest
import site.addzero.kcloud.plugins.system.pluginmarket.model.RunPluginBuildRequest
import site.addzero.kcloud.plugins.system.pluginmarket.service.PluginDeploymentService

/**
 * 导出插件模块并改写受管接线。
 */
@PostMapping("/api/kcloud/plugin-market/packages/{id}/deploy")
suspend fun deployPluginPackage(
    @PathVariable id: String,
    @RequestParam("runBuild") runBuild: Boolean?,
): PluginDeploymentJobDto {
    return deploymentService().deploy(
        DeployPluginPackageRequest(
            packageId = id,
            runBuild = runBuild,
        ),
    )
}

/**
 * 手动触发插件验证构建。
 */
@PostMapping("/api/kcloud/plugin-market/packages/{id}/build")
suspend fun buildPluginPackage(
    @PathVariable id: String,
): PluginDeploymentJobDto {
    return deploymentService().runBuild(RunPluginBuildRequest(packageId = id))
}

/**
 * 读取插件部署与构建任务。
 */
@GetMapping("/api/kcloud/plugin-market/jobs")
suspend fun listPluginDeploymentJobs(
    @RequestParam("packageId") packageId: String?,
): List<PluginDeploymentJobDto> {
    return deploymentService().listJobs(packageId)
}

private fun deploymentService(): PluginDeploymentService = KoinPlatform.getKoin().get()
