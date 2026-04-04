package site.addzero.kcloud.plugins.mcuconsole.api.external

import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.mp.KoinPlatform
import site.addzero.kcloud.plugins.mcuconsole.api.external.*

/**
 * 聚合后的 Ktorfit 服务提供者
 *
 * 仅聚合 controller2api 生成的接口，不扫描手写接口。
 */
object Apis {
    private fun ktorfit(): Ktorfit = KoinPlatform.getKoin().get()

    /**
     * McuDeviceInfoApi 服务实例
     */
    val mcuDeviceInfoApi
        get() = ktorfit().createMcuDeviceInfoApi()

    /**
     * McuFlashApi 服务实例
     */
    val mcuFlashApi
        get() = ktorfit().createMcuFlashApi()

    /**
     * McuModbusDeviceRoutesApi 服务实例
     */
    val mcuModbusDeviceRoutesApi
        get() = ktorfit().createMcuModbusDeviceRoutesApi()

    /**
     * McuModbusDeviceWriteApi 服务实例
     */
    val mcuModbusDeviceWriteApi
        get() = ktorfit().createMcuModbusDeviceWriteApi()

    /**
     * McuRuntimeApi 服务实例
     */
    val mcuRuntimeApi
        get() = ktorfit().createMcuRuntimeApi()

    /**
     * McuScriptApi 服务实例
     */
    val mcuScriptApi
        get() = ktorfit().createMcuScriptApi()

    /**
     * McuSessionApi 服务实例
     */
    val mcuSessionApi
        get() = ktorfit().createMcuSessionApi()

    /**
     * McuSettingsApi 服务实例
     */
    val mcuSettingsApi
        get() = ktorfit().createMcuSettingsApi()
}