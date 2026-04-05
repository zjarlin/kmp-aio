package site.addzero.kcloud.server

import io.ktor.server.config.ApplicationConfig
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.configcenter.ConfigCenterBeanFactory
import site.addzero.configcenter.ConfigCenterEnv
import site.addzero.configcenter.ConfigCenterJdbcSettings
import site.addzero.configcenter.configCenterJdbcSettingsOrNull
import site.addzero.configcenter.env
import site.addzero.kcloud.plugins.system.configcenter.spi.RUNTIME_CONFIG_CENTER_ACTIVE_KEY
import site.addzero.kcloud.plugins.system.configcenter.spi.RuntimeConfigCenterActive
import site.addzero.kcloud.plugins.system.configcenter.spi.requireRuntimeConfigCenterActive

@Module
class ServerRuntimeConfigCenterModule {
    @Single(createdAtStart = true)
    fun provideConfigCenterJdbcSettings(
        applicationConfig: ApplicationConfig,
    ): ConfigCenterJdbcSettings {
        return applicationConfig.configCenterJdbcSettingsOrNull()
            ?: error(
                "缺少配置中心 JDBC，无法初始化运行时 ConfigCenterEnv。" +
                    "请提供 config-center.jdbc.* 或 datasources.sqlite/postgres.*。",
            )
    }

    @Single(createdAtStart = true)
    fun provideRuntimeConfigCenterActive(
        applicationConfig: ApplicationConfig,
    ): RuntimeConfigCenterActive {
        return RuntimeConfigCenterActive(
            value = requireRuntimeConfigCenterActive(
                applicationConfig.propertyOrNull(RUNTIME_CONFIG_CENTER_ACTIVE_KEY)?.getString(),
            ),
        )
    }

    @Single(createdAtStart = true)
    fun provideRuntimeConfigCenterEnv(
        jdbcSettings: ConfigCenterJdbcSettings,
        runtimeActive: RuntimeConfigCenterActive,
    ): ConfigCenterEnv {
        return ConfigCenterBeanFactory.env(
            settings = jdbcSettings,
            namespace = KCLOUD_CONFIG_CENTER_NAMESPACE,
            active = runtimeActive.value,
        )
    }
}
