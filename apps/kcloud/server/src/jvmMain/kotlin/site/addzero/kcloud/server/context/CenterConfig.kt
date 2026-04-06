package site.addzero.kcloud.server.context

import io.ktor.server.config.ApplicationConfig
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.configcenter.ConfigCenterBeanFactory
import site.addzero.configcenter.ConfigCenterEnv
import site.addzero.configcenter.ConfigCenterJdbcSettings
import site.addzero.configcenter.jdbc
import site.addzero.kcloud.plugins.system.configcenter.spi.RuntimeConfigCenterActive
import site.addzero.kcloud.server.KCLOUD_CONFIG_CENTER_NAMESPACE
import site.addzero.kcloud.server.resolveConfigCenterActive
import site.addzero.kcloud.server.resolveConfigCenterJdbcSettings

@Module
class CenterConfig {
    @Single(createdAtStart = true)
    fun configCenterJdbcSettings(
        config: ApplicationConfig,
    ): ConfigCenterJdbcSettings {
        return resolveConfigCenterJdbcSettings(config)
    }

    @Single(createdAtStart = true)
    fun runtimeConfigCenterActive(
        config: ApplicationConfig,
    ): RuntimeConfigCenterActive {
        val active = resolveConfigCenterActive(config)
        return object : RuntimeConfigCenterActive {
            override val value: String = active
        }
    }

    @Single(createdAtStart = true)
    fun configCenterBeanFactory(
        settings: ConfigCenterJdbcSettings,
    ): ConfigCenterBeanFactory {
        return ConfigCenterBeanFactory.jdbc(settings)
    }

    @Single(createdAtStart = true)
    fun env(
        beanFactory: ConfigCenterBeanFactory,
        runtimeConfigCenterActive: RuntimeConfigCenterActive,
    ): ConfigCenterEnv {
        return beanFactory.env(
            namespace = KCLOUD_CONFIG_CENTER_NAMESPACE,
            active = runtimeConfigCenterActive.value,
        )
    }
}
