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
    val mcuDeviceInfoApi: McuDeviceInfoApi
        get() = ktorfit().createMcuDeviceInfoApi()

    /**
     * McuFlashApi 服务实例
     */
    val mcuFlashApi: McuFlashApi
        get() = ktorfit().createMcuFlashApi()

    /**
     * McuModbusAtomicRoutesApi 服务实例
     */
    val mcuModbusAtomicRoutesApi: McuModbusAtomicRoutesApi
        get() = ktorfit().createMcuModbusAtomicRoutesApi()

    /**
     * McuModbusDeviceRoutesApi 服务实例
     */
    val mcuModbusDeviceRoutesApi: McuModbusDeviceRoutesApi
        get() = ktorfit().createMcuModbusDeviceRoutesApi()

    /**
     * McuModbusDeviceWriteApi 服务实例
     */
    val mcuModbusDeviceWriteApi: McuModbusDeviceWriteApi
        get() = ktorfit().createMcuModbusDeviceWriteApi()

    /**
     * McuRuntimeApi 服务实例
     */
    val mcuRuntimeApi: McuRuntimeApi
        get() = ktorfit().createMcuRuntimeApi()

    /**
     * McuScriptApi 服务实例
     */
    val mcuScriptApi: McuScriptApi
        get() = ktorfit().createMcuScriptApi()

    /**
     * McuSessionApi 服务实例
     */
    val mcuSessionApi: McuSessionApi
        get() = ktorfit().createMcuSessionApi()

    /**
     * McuSettingsApi 服务实例
     */
    val mcuSettingsApi: McuSettingsApi
        get() = ktorfit().createMcuSettingsApi()
}