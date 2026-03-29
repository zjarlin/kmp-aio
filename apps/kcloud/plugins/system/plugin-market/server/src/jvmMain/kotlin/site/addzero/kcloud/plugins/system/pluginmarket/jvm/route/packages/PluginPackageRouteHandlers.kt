@file:JvmName("PluginPackageRouteHandlers")

package site.addzero.kcloud.plugins.system.pluginmarket.jvm.route.packages

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.*
import site.addzero.kcloud.plugins.system.pluginmarket.model.*
import site.addzero.kcloud.plugins.system.pluginmarket.service.PluginPackageService

/**
 * 插件包管理路由。
 */
@GetMapping("/api/kcloud/plugin-market/packages")
suspend fun listPluginPackages(
    @RequestParam("query") query: String?,
): List<PluginPackageDto> {
    return packageService().list(PluginMarketSearchRequest(query = query))
}

/**
 * 创建新的数据库托管插件包。
 */
@PostMapping("/api/kcloud/plugin-market/packages")
suspend fun createPluginPackage(
    @RequestBody request: CreatePluginPackageRequest,
): PluginPackageAggregateDto {
    return packageService().create(request)
}

/**
 * 读取插件包详情。
 */
@GetMapping("/api/kcloud/plugin-market/packages/{id}")
suspend fun getPluginPackage(
    @PathVariable id: String,
): PluginPackageDto {
    return packageService().get(id)
}

/**
 * 读取插件包聚合详情。
 */
@GetMapping("/api/kcloud/plugin-market/packages/{id}/aggregate")
suspend fun getPluginPackageAggregate(
    @PathVariable id: String,
): PluginPackageAggregateDto {
    return packageService().aggregate(id)
}

/**
 * 更新插件包元信息。
 */
@PutMapping("/api/kcloud/plugin-market/packages/{id}")
suspend fun updatePluginPackage(
    @PathVariable id: String,
    @RequestBody request: UpdatePluginPackageRequest,
): PluginPackageDto {
    return packageService().update(id, request)
}

/**
 * 读取插件包删除前检查信息。
 */
@GetMapping("/api/kcloud/plugin-market/packages/{id}/delete-check")
suspend fun deletePluginPackageCheck(
    @PathVariable id: String,
): PluginDeleteCheckResultDto {
    return packageService().deleteCheck(id)
}

/**
 * 删除数据库托管插件包。
 */
@DeleteMapping("/api/kcloud/plugin-market/packages/{id}")
suspend fun deletePluginPackage(
    @PathVariable id: String,
) {
    packageService().delete(id)
}

private fun packageService(): PluginPackageService = KoinPlatform.getKoin().get()
