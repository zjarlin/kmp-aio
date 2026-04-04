package site.addzero.kcloud.api

import de.jensklingenberg.ktorfit.Ktorfit
import site.addzero.kcloud.api.*

/**
 * 聚合后的 Ktorfit 服务提供者
 *
 * 仅聚合 controller2api 生成的接口，不扫描手写接口。
 */
object ApiProvider {
    private var currentKtorfit: Ktorfit? = null

    fun configure(ktorfit: Ktorfit) {
        currentKtorfit = ktorfit
    }

    private fun requireKtorfit(): Ktorfit {
        return currentKtorfit
            ?: error("ApiProvider 尚未配置 Ktorfit，请先调用 ApiProvider.configure(ktorfit)")
    }

    /**
     * ConfigApi 服务实例
     */
    val configApi
        get() = requireKtorfit().create<ConfigApi>()

    /**
     * FavoriteApi 服务实例
     */
    val favoriteApi
        get() = requireKtorfit().create<FavoriteApi>()

    /**
     * HistoryApi 服务实例
     */
    val historyApi
        get() = requireKtorfit().create<HistoryApi>()

    /**
     * MusicSearchApi 服务实例
     */
    val musicSearchApi
        get() = requireKtorfit().create<MusicSearchApi>()

    /**
     * PersonaApi 服务实例
     */
    val personaApi
        get() = requireKtorfit().create<PersonaApi>()

    /**
     * SunoRoutesApi 服务实例
     */
    val sunoRoutesApi
        get() = requireKtorfit().create<SunoRoutesApi>()

    /**
     * SunoTaskResourceApi 服务实例
     */
    val sunoTaskResourceApi
        get() = requireKtorfit().create<SunoTaskResourceApi>()
}
