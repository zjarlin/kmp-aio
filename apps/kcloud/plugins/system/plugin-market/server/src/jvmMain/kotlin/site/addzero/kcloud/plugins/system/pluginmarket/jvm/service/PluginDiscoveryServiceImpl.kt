package site.addzero.kcloud.plugins.system.pluginmarket.jvm.service

import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.*
import site.addzero.kcloud.plugins.system.pluginmarket.model.ImportDiscoveredPluginRequest
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginDiscoveryItemDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginMarketSearchRequest
import site.addzero.kcloud.plugins.system.pluginmarket.service.PluginDiscoveryService
import java.nio.file.Paths

@Single(
    binds = [
        PluginDiscoveryService::class,
    ],
)
class PluginDiscoveryServiceImpl(
    private val sqlClient: KSqlClient,
    private val catalog: PluginMarketCatalogSupport,
    private val aggregateSupport: PluginPackageAggregateSupport,
    private val discoverySupport: PluginModuleDiscoverySupport,
    private val workspaceSupport: PluginPackageWorkspaceSupport,
) : PluginDiscoveryService {
    override suspend fun discover(search: PluginMarketSearchRequest): List<PluginDiscoveryItemDto> {
        return discoverySupport.discoverPluginModules(search.query)
    }

    override suspend fun importDiscovered(request: ImportDiscoveredPluginRequest): site.addzero.kcloud.plugins.system.pluginmarket.model.PluginPackageAggregateDto {
        val discovery = discoverySupport.discoverPluginModules()
            .firstOrNull { it.discoveryId == request.discoveryId }
            ?: throw NoSuchElementException("未找到发现模块: ${request.discoveryId}")
        require(discovery.issues.isEmpty()) {
            "发现模块结构不完整，暂不支持导入：${discovery.issues.joinToString("；")}"
        }
        require(
            catalog.listPackages().none { it.pluginId.equals(request.managedPluginId.trim(), ignoreCase = true) }
        ) { "插件 ID 已存在: ${request.managedPluginId.trim()}" }
        val now = catalog.now()
        val packageId = catalog.newId()
        val pluginPackage = new(PluginPackage::class).by {
            id = packageId
            pluginId = request.managedPluginId.trim()
            name = request.managedName.trim()
            description = "从磁盘导入: ${discovery.moduleDir}"
            version = "0.1.0"
            pluginGroup = discovery.pluginGroup
            enabled = true
            moduleDir = discovery.moduleDir
            basePackage = discovery.packageName?.substringBeforeLast(".").orEmpty().ifBlank {
                "site.addzero.kcloud.plugins.${request.managedPluginId.trim().toPascalCase().lowercase()}"
            }
            managedByDb = true
            composeKoinModuleClass = discovery.composeKoinModuleClass
            serverKoinModuleClass = discovery.serverKoinModuleClass
            routeRegistrarImport = discovery.routeRegistrarImport
            routeRegistrarCall = discovery.routeRegistrarCall
            createdAt = now
            updatedAt = now
        }
        sqlClient.save(pluginPackage)
        workspaceSupport.collectImportableFiles(Paths.get(discovery.moduleDir)).forEachIndexed { index, (relativePath, content) ->
            sqlClient.save(
                new(PluginSourceFile::class).by {
                    id = catalog.newId()
                    this.pluginPackage = catalog.packageRef(packageId)
                    this.relativePath = relativePath
                    this.content = content
                    contentHash = catalog.hashContent(content)
                    fileGroup = inferFileGroup(relativePath)
                    readOnly = false
                    orderIndex = index
                    createdAt = now
                    updatedAt = now
                },
            )
        }
        sqlClient.save(
            new(PluginImportRecord::class).by {
                id = catalog.newId()
                this.pluginPackage = catalog.packageRef(packageId)
                sourceModuleDir = discovery.moduleDir
                sourceGradlePath = discovery.gradlePath
                importedAt = now
            },
        )
        return aggregateSupport.buildAggregate(packageId)
    }

    private fun inferFileGroup(path: String): String {
        return when {
            path.startsWith("src/commonMain") -> "common"
            path.startsWith("src/jvmMain") -> "jvm"
            else -> "meta"
        }
    }
}
