package site.addzero.kcloud.api

import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.mp.KoinPlatform
import site.addzero.kcloud.api.*

/**
 * 聚合后的 Ktorfit 服务提供者
 *
 * 仅聚合 controller2api 生成的接口，不扫描手写接口。
 */
object Apis {
    private fun ktorfit(): Ktorfit = KoinPlatform.getKoin().get()

    /**
     * ConfigApi 服务实例
     */
    val configApi: ConfigApi
        get() = ktorfit().createConfigApi()

    /**
     * FavoriteApi 服务实例
     */
    val favoriteApi: FavoriteApi
        get() = ktorfit().createFavoriteApi()

    /**
     * HistoryApi 服务实例
     */
    val historyApi: HistoryApi
        get() = ktorfit().createHistoryApi()

    /**
     * MusicSearchApi 服务实例
     */
    val musicSearchApi: MusicSearchApi
        get() = ktorfit().createMusicSearchApi()

    /**
     * PersonaApi 服务实例
     */
    val personaApi: PersonaApi
        get() = ktorfit().createPersonaApi()

    /**
     * SunoRoutesApi 服务实例
     */
    val sunoRoutesApi: SunoRoutesApi
        get() = ktorfit().createSunoRoutesApi()

    /**
     * SunoTaskResourceApi 服务实例
     */
    val sunoTaskResourceApi: SunoTaskResourceApi
        get() = ktorfit().createSunoTaskResourceApi()
}