package site.addzero.kcloud.plugins.system.pluginmarket.service

import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginSourceFileDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.SavePluginSourceFileRequest

interface PluginSourceFileService {
    suspend fun list(packageId: String): List<PluginSourceFileDto>

    suspend fun save(request: SavePluginSourceFileRequest): PluginSourceFileDto

    suspend fun delete(fileId: String)
}
