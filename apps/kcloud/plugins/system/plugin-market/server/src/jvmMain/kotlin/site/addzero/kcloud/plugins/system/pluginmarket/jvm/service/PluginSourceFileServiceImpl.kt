package site.addzero.kcloud.plugins.system.pluginmarket.jvm.service

import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.*
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginSourceFileDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.SavePluginSourceFileRequest
import site.addzero.kcloud.plugins.system.pluginmarket.service.PluginSourceFileService

@Single(
    binds = [
        PluginSourceFileService::class,
    ],
)
class PluginSourceFileServiceImpl(
    private val sqlClient: KSqlClient,
    private val catalog: PluginMarketCatalogSupport,
) : PluginSourceFileService {
    override suspend fun list(packageId: String): List<PluginSourceFileDto> {
        catalog.packageOrThrow(packageId)
        return catalog.listFiles(packageId).map { it.toDto() }
    }

    override suspend fun save(request: SavePluginSourceFileRequest): PluginSourceFileDto {
        require(request.relativePath.isNotBlank()) { "相对路径不能为空" }
        val pluginPackage = catalog.packageOrThrow(request.packageId)
        val existing = catalog.listFiles(request.packageId).firstOrNull { it.relativePath == request.relativePath }
        val now = catalog.now()
        val entity = new(PluginSourceFile::class).by {
            id = existing?.id ?: catalog.newId()
            this.pluginPackage = catalog.packageRef(pluginPackage.id)
            relativePath = request.relativePath.trim()
            content = request.content
            contentHash = catalog.hashContent(request.content)
            fileGroup = request.fileGroup.trim().ifBlank { "source" }
            readOnly = existing?.readOnly ?: false
            orderIndex = existing?.orderIndex ?: ((catalog.listFiles(request.packageId).maxOfOrNull { it.orderIndex } ?: -1) + 1)
            createdAt = existing?.createdAt ?: now
            updatedAt = now
        }
        return sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun delete(fileId: String) {
        catalog.fileOrThrow(fileId)
        sqlClient.deleteById(PluginSourceFile::class, fileId)
    }
}
