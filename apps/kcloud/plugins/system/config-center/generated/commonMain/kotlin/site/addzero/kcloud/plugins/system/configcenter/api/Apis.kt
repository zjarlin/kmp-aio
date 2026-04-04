package site.addzero.kcloud.plugins.system.configcenter.api

import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.mp.KoinPlatform
import site.addzero.kcloud.plugins.system.configcenter.api.*

/**
 * 聚合后的 Ktorfit 服务提供者
 *
 * 仅聚合 controller2api 生成的接口，不扫描手写接口。
 */
object Apis {
    private fun ktorfit(): Ktorfit = KoinPlatform.getKoin().get()

    /**
     * ConfigCenterApi 服务实例
     */
    val configCenterApi
        get() = ktorfit().createConfigCenterApi()
}