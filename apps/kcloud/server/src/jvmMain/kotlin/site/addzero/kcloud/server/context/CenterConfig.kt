package site.addzero.kcloud.server.context

import site.addzero.configcenter.ConfigCenterEnv
import site.addzero.configcenter.ConfigCenterBeanFactory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class CenterConfig {
    @Single(createdAtStart = true)
    fun env(
    ): ConfigCenterEnv {
        return ConfigCenterBeanFactory.env(
            url = "jdbc:sqlite:./config-center.sqlite",
            namespace = "kcloud",
            active = "dev",
        )
    }
}
