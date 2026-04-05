import site.addzero.configcenter.ConfigCenterEnv
import site.addzero.configcenter.ConfigCenterBeanFactory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.configcenter.env

@Module
class ConfigCenterModule {
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