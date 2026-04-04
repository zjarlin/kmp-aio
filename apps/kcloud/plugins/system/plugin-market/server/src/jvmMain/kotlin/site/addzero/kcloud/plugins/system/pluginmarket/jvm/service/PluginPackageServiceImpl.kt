package site.addzero.kcloud.plugins.system.pluginmarket.jvm.service

import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.*
import site.addzero.kcloud.plugins.system.pluginmarket.model.CreatePluginPackageRequest
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginDeleteCheckResultDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginPackageAggregateDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginPackageDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginMarketSearchRequest
import site.addzero.kcloud.plugins.system.pluginmarket.model.UpdatePluginPackageRequest
import site.addzero.kcloud.plugins.system.pluginmarket.service.PluginPackageService
import site.addzero.kcloud.plugins.system.pluginmarket.service.PluginPresetService

@Single
class PluginPackageServiceImpl(
    private val sqlClient: KSqlClient,
    private val catalog: PluginMarketCatalogSupport,
    private val aggregateSupport: PluginPackageAggregateSupport,
    private val workspaceSupport: PluginPackageWorkspaceSupport,
    private val presetService: PluginPresetService,
) : PluginPackageService {
    override suspend fun create(request: CreatePluginPackageRequest): PluginPackageAggregateDto {
        require(request.pluginId.isNotBlank()) { "插件 ID 不能为空" }
        require(request.name.isNotBlank()) { "插件名称不能为空" }
        require(request.basePackage.isNotBlank()) { "基础包名不能为空" }
        if (catalog.listPackages().any { it.pluginId.equals(request.pluginId, ignoreCase = true) }) {
            throw IllegalArgumentException("插件 ID 已存在: ${request.pluginId}")
        }
        val now = catalog.now()
        val entity = new(PluginPackage::class).by {
            id = catalog.newId()
            pluginId = request.pluginId.trim()
            name = request.name.trim()
            description = request.description?.trim()?.ifBlank { null }
            version = request.version.trim().ifBlank { "0.1.0" }
            pluginGroup = request.pluginGroup?.trim()?.ifBlank { null }
            enabled = request.enabled
            moduleDir = request.moduleDir?.trim()?.ifBlank { defaultModuleDir(request) } ?: defaultModuleDir(request)
            basePackage = request.basePackage.trim()
            managedByDb = true
            composeKoinModuleClass = request.composeKoinModuleClass?.trim()?.ifBlank {
                defaultComposeKoinModuleClass(request)
            } ?: defaultComposeKoinModuleClass(request)
            serverKoinModuleClass = request.serverKoinModuleClass?.trim()?.ifBlank {
                defaultServerKoinModuleClass(request)
            } ?: defaultServerKoinModuleClass(request)
            routeRegistrarImport = request.routeRegistrarImport?.trim()?.ifBlank {
                defaultRouteRegistrarImport(request)
            } ?: defaultRouteRegistrarImport(request)
            routeRegistrarCall = request.routeRegistrarCall?.trim()?.ifBlank {
                defaultRouteRegistrarCall(request)
            } ?: defaultRouteRegistrarCall(request)
            createdAt = now
            updatedAt = now
        }
        val saved = sqlClient.save(entity).modifiedEntity
        presetService.applyPreset(saved.id, request.presetKind)
        return aggregateSupport.buildAggregate(saved.id)
    }

    override suspend fun list(search: PluginMarketSearchRequest): List<PluginPackageDto> {
        return catalog.listPackages()
            .filter { plugin ->
                !search.managedOnly || plugin.managedByDb
            }
            .filter { plugin ->
                val query = search.query
                query.isNullOrBlank() ||
                    plugin.pluginId.contains(query, ignoreCase = true) ||
                    plugin.name.contains(query, ignoreCase = true) ||
                    plugin.moduleDir.contains(query, ignoreCase = true)
            }
            .map { aggregateSupport.packageDto(it) }
    }

    override suspend fun get(id: String): PluginPackageDto {
        return aggregateSupport.packageDto(catalog.packageOrThrow(id))
    }

    override suspend fun aggregate(id: String): PluginPackageAggregateDto {
        return aggregateSupport.buildAggregate(id)
    }

    override suspend fun update(id: String, request: UpdatePluginPackageRequest): PluginPackageDto {
        val existing = catalog.packageOrThrow(id)
        val entity = new(PluginPackage::class).by {
            this.id = id
            pluginId = existing.pluginId
            name = request.name.trim()
            description = request.description?.trim()?.ifBlank { null }
            version = request.version.trim().ifBlank { "0.1.0" }
            pluginGroup = request.pluginGroup?.trim()?.ifBlank { null }
            enabled = request.enabled
            moduleDir = request.moduleDir.trim()
            basePackage = request.basePackage.trim()
            managedByDb = existing.managedByDb
            composeKoinModuleClass = request.composeKoinModuleClass?.trim()?.ifBlank { null }
            serverKoinModuleClass = request.serverKoinModuleClass?.trim()?.ifBlank { null }
            routeRegistrarImport = request.routeRegistrarImport?.trim()?.ifBlank { null }
            routeRegistrarCall = request.routeRegistrarCall?.trim()?.ifBlank { null }
            createdAt = existing.createdAt
            updatedAt = catalog.now()
        }
        val saved = sqlClient.save(entity).modifiedEntity
        workspaceSupport.syncEnabledMarker(saved)
        return aggregateSupport.packageDto(saved)
    }

    override suspend fun enable(id: String): PluginPackageDto {
        val existing = catalog.packageOrThrow(id)
        if (existing.enabled) {
            workspaceSupport.syncEnabledMarker(existing)
            return aggregateSupport.packageDto(existing)
        }
        return update(
            id,
            UpdatePluginPackageRequest(
                name = existing.name,
                pluginGroup = existing.pluginGroup,
                description = existing.description,
                version = existing.version,
                basePackage = existing.basePackage,
                moduleDir = existing.moduleDir,
                enabled = true,
                composeKoinModuleClass = existing.composeKoinModuleClass,
                serverKoinModuleClass = existing.serverKoinModuleClass,
                routeRegistrarImport = existing.routeRegistrarImport,
                routeRegistrarCall = existing.routeRegistrarCall,
            ),
        )
    }

    override suspend fun disable(id: String): PluginPackageDto {
        val existing = catalog.packageOrThrow(id)
        if (!existing.enabled) {
            workspaceSupport.syncEnabledMarker(existing)
            return aggregateSupport.packageDto(existing)
        }
        return update(
            id,
            UpdatePluginPackageRequest(
                name = existing.name,
                pluginGroup = existing.pluginGroup,
                description = existing.description,
                version = existing.version,
                basePackage = existing.basePackage,
                moduleDir = existing.moduleDir,
                enabled = false,
                composeKoinModuleClass = existing.composeKoinModuleClass,
                serverKoinModuleClass = existing.serverKoinModuleClass,
                routeRegistrarImport = existing.routeRegistrarImport,
                routeRegistrarCall = existing.routeRegistrarCall,
            ),
        )
    }

    override suspend fun deleteCheck(id: String): PluginDeleteCheckResultDto {
        val aggregate = aggregateSupport.buildAggregate(id)
        return PluginDeleteCheckResultDto(
            id = id,
            canDelete = true,
            warnings = listOf(
                "将删除 ${aggregate.files.size} 个源码文件",
                "将删除 ${aggregate.jobs.size} 条部署记录",
            ),
        )
    }

    override suspend fun delete(id: String) {
        val pluginPackage = catalog.packageOrThrow(id)
        sqlClient.transaction {
            if (pluginPackage.managedByDb) {
                workspaceSupport.uninstallManagedModule(pluginPackage)
            }
            catalog.deletePackageCascade(id)
        }
    }

    override suspend fun uninstall(id: String) {
        delete(id)
    }

    private fun defaultModuleDir(request: CreatePluginPackageRequest): String {
        val groupSegment = request.pluginGroup?.trim()?.ifBlank { null }?.replace(".", "/")
        return buildString {
            append("apps/kcloud/plugins")
            if (groupSegment != null) {
                append("/")
                append(groupSegment)
            }
            append("/")
            append(request.pluginId.trim())
        }
    }

    private fun defaultComposeKoinModuleClass(request: CreatePluginPackageRequest): String {
        return "${defaultPluginPackage(request)}.${request.pluginId.trim().toPascalCase()}ComposeKoinModule"
    }

    private fun defaultServerKoinModuleClass(request: CreatePluginPackageRequest): String {
        return "${defaultPluginPackage(request)}.${request.pluginId.trim().toPascalCase()}ServerKoinModule"
    }

    private fun defaultRouteRegistrarImport(request: CreatePluginPackageRequest): String {
        return "${defaultPluginPackage(request)}.${request.pluginId.trim().camelCase()}Routes"
    }

    private fun defaultRouteRegistrarCall(request: CreatePluginPackageRequest): String {
        return "${request.pluginId.trim().camelCase()}Routes()"
    }

    private fun defaultPluginPackage(request: CreatePluginPackageRequest): String {
        return request.basePackage.trim().appendSegment(request.pluginId.trim().toPascalCase().lowercase())
    }
}
