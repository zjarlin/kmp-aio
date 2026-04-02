package site.addzero.configcenter

import io.ktor.server.config.ApplicationConfig
import org.koin.core.annotation.Single
import java.util.logging.Logger

private val configCenterMetadataLogger: Logger =
    Logger.getLogger(ConfigCenterMetadataBootstrap::class.java.name)

@Single(createdAtStart = true)
class ConfigCenterMetadataBootstrap(
    private val applicationConfig: ApplicationConfig,
    private val definitionProviders: List<ConfigCenterDefinitionProvider>,
) {
    init {
        val settings = applicationConfig.configCenterJdbcSettingsOrNull()
        if (settings != null && definitionProviders.isNotEmpty()) {
            val definitions = definitionProviders
                .flatMap(ConfigCenterDefinitionProvider::definitions)
                .distinctBy { definition -> "${definition.namespace}::${definition.key}" }
            if (definitions.isNotEmpty()) {
                val syncedCount = JdbcConfigCenterValueService(settings).upsertDefinitions(definitions)
                configCenterMetadataLogger.info(
                    "已同步配置中心元数据 $syncedCount 项，可在内置 H5 管理页维护 value/comment/default/required。",
                )
            }
        }
    }
}
