package site.addzero.kcloud.plugins.mcuconsole.api.external

import de.jensklingenberg.ktorfit.Ktorfit
import site.addzero.kcloud.plugins.mcuconsole.api.external.*

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
     * McuDeviceInfoApi 服务实例
     */
    val mcuDeviceInfoApi: McuDeviceInfoApi
        get() = requireKtorfit().create<McuDeviceInfoApi>()

    /**
     * McuFlashApi 服务实例
     */
    val mcuFlashApi: McuFlashApi
        get() = requireKtorfit().create<McuFlashApi>()

    /**
     * McuModbusDeviceRoutesApi 服务实例
     */
    val mcuModbusDeviceRoutesApi: McuModbusDeviceRoutesApi
        get() = requireKtorfit().create<McuModbusDeviceRoutesApi>()

    /**
     * McuRuntimeApi 服务实例
     */
    val mcuRuntimeApi: McuRuntimeApi
        get() = requireKtorfit().create<McuRuntimeApi>()

    /**
     * McuScriptApi 服务实例
     */
    val mcuScriptApi: McuScriptApi
        get() = requireKtorfit().create<McuScriptApi>()

    /**
     * McuSessionApi 服务实例
     */
    val mcuSessionApi: McuSessionApi
        get() = requireKtorfit().create<McuSessionApi>()

    /**
     * McuSettingsApi 服务实例
     */
    val mcuSettingsApi: McuSettingsApi
        get() = requireKtorfit().create<McuSettingsApi>()
}