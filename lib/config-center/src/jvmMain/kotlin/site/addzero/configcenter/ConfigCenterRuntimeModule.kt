package site.addzero.configcenter

import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class ConfigCenterRuntimeModule {
    @Single(createdAtStart = true)
    fun provideJdbcConfigCenterValueService(
        settings: ConfigCenterJdbcSettings,
    ): JdbcConfigCenterValueService {
        return JdbcConfigCenterValueService(settings)
    }

    @Single(createdAtStart = true)
    fun provideConfigCenterValueService(
        jdbcConfigCenterValueService: JdbcConfigCenterValueService,
    ): ConfigCenterValueService {
        return jdbcConfigCenterValueService
    }

    @Single(createdAtStart = true)
    fun provideConfigCenterAdminService(
        jdbcConfigCenterValueService: JdbcConfigCenterValueService,
    ): ConfigCenterAdminService {
        return jdbcConfigCenterValueService
    }

    @Single(createdAtStart = true)
    fun provideConfigCenterMetadataBootstrap(
        definitionProviders: List<ConfigCenterDefinitionProvider>,
        jdbcConfigCenterValueService: JdbcConfigCenterValueService,
    ): ConfigCenterMetadataBootstrap {
        return ConfigCenterMetadataBootstrap(
            definitionProviders = definitionProviders,
            jdbcConfigCenterValueService = jdbcConfigCenterValueService,
        )
    }
}
