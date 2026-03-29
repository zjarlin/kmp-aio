package site.addzero.kcloud.plugins.system.pluginmarket.jvm.route.file

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginSourceFileDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.SavePluginSourceFileRequest
import site.addzero.kcloud.plugins.system.pluginmarket.service.PluginSourceFileService

/**
 * 读取指定插件包的源码文件列表。
 */
@GetMapping("/api/kcloud/plugin-market/packages/{id}/files")
suspend fun listPluginSourceFiles(
    @PathVariable id: String,
): List<PluginSourceFileDto> {
    return sourceFileService().list(id)
}

/**
 * 插件源码文件写入路由。
 */
@PostMapping("/api/kcloud/plugin-market/files")
suspend fun savePluginSourceFile(
    @RequestBody request: SavePluginSourceFileRequest,
): PluginSourceFileDto {
    return sourceFileService().save(request)
}

/**
 * 删除插件源码文件。
 */
@DeleteMapping("/api/kcloud/plugin-market/files/{id}")
suspend fun deletePluginSourceFile(
    @PathVariable id: String,
) {
    sourceFileService().delete(id)
}

private fun sourceFileService(): PluginSourceFileService = KoinPlatform.getKoin().get()
