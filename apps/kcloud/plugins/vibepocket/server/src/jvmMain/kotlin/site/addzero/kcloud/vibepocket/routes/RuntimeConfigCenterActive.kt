package site.addzero.kcloud.vibepocket.routes

import org.koin.mp.KoinPlatform
import site.addzero.kcloud.plugins.system.configcenter.spi.RuntimeConfigCenterActive

internal fun currentRuntimeConfigCenterActive(): String {
    return KoinPlatform.getKoin().get<RuntimeConfigCenterActive>().value
}
