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
}
