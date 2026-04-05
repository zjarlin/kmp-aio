package site.addzero.kcloud.vibepocket.routes

import io.ktor.server.config.ApplicationConfig
import org.koin.mp.KoinPlatform
import site.addzero.kcloud.plugins.system.configcenter.spi.RUNTIME_CONFIG_CENTER_ACTIVE_KEY
import site.addzero.kcloud.plugins.system.configcenter.spi.requireRuntimeConfigCenterActive

internal fun currentRuntimeConfigCenterActive(): String {
    val applicationConfig = KoinPlatform.getKoin().get<ApplicationConfig>()
    return requireRuntimeConfigCenterActive(
        applicationConfig.propertyOrNull(RUNTIME_CONFIG_CENTER_ACTIVE_KEY)?.getString(),
    )
}
