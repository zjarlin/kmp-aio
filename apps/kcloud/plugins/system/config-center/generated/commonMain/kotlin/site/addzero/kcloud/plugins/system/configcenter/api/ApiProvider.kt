package site.addzero.kcloud.plugins.system.configcenter.api

import de.jensklingenberg.ktorfit.Ktorfit
import site.addzero.kcloud.plugins.system.configcenter.api.*

/**
 * 聚合后的 Ktorfit 服务提供者
 *
 * 仅聚合 controller2api 生成的接口，不扫描手写接口。
 */
object ApiProvider {
    @Volatile
    private var currentKtorfit: Ktorfit? = null

    fun configure(ktorfit: Ktorfit) {
        currentKtorfit = ktorfit
    }

    private fun requireKtorfit(): Ktorfit {
        return currentKtorfit
            ?: error("ApiProvider 尚未配置 Ktorfit，请先调用 ApiProvider.configure(ktorfit)")
    }

    /**
     * ConfigCenterApi 服务实例
     */
    val configCenterApi: ConfigCenterApi
        get() = requireKtorfit().create<ConfigCenterApi>()
}