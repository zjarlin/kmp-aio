package site.addzero.configcenter

import java.util.logging.Logger

private val configCenterMetadataLogger: Logger =
    Logger.getLogger(ConfigCenterMetadataBootstrap::class.java.name)

class ConfigCenterMetadataBootstrap(
    private val definitionProviders: List<ConfigCenterDefinitionProvider>,
    private val jdbcConfigCenterValueService: JdbcConfigCenterValueService,
) {
    init {
        if (definitionProviders.isNotEmpty()) {
            syncDefinitions()
        }
    }

    private fun syncDefinitions() {
        val definitions = definitionProviders
            .flatMap(ConfigCenterDefinitionProvider::definitions)
            .distinctBy { definition -> "${definition.namespace}::${definition.key}" }
        if (definitions.isEmpty()) {
            return
        }
        val syncedCount = jdbcConfigCenterValueService.upsertDefinitions(definitions)
        configCenterMetadataLogger.info(
            "已同步配置中心元数据 $syncedCount 项，可在内置 H5 管理页维护 value/comment/default/required。",
        )
    }
}
