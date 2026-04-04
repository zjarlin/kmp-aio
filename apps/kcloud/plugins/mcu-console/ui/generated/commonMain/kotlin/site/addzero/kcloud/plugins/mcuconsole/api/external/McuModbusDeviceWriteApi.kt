package site.addzero.kcloud.plugins.mcuconsole.api.external

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.plugins.mcuconsole.modbus.device.McuModbusIndicatorLightsResponse
import site.addzero.kcloud.plugins.mcuconsole.modbus.device.McuModbusIndicatorLightsRequest

/**
 * 原始文件: site.addzero.kcloud.plugins.mcuconsole.modbus.device.McuModbusDeviceWrite.kt
 * 基础路径: 
 */
interface McuModbusDeviceWriteApi {

/**
 * writeMcuModbusIndicatorLights
 * HTTP方法: POST
 * 路径: /api/mcu/modbus/indicator-lights
 * 参数:
 *   - request: site.addzero.kcloud.plugins.mcuconsole.modbus.device.McuModbusIndicatorLightsRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.modbus.device.McuModbusIndicatorLightsResponse
 */
    @POST("/api/mcu/modbus/indicator-lights")
    @Headers("Content-Type: application/json")
    suspend fun writeMcuModbusIndicatorLights(
        @Body request: site.addzero.kcloud.plugins.mcuconsole.modbus.device.McuModbusIndicatorLightsRequest
    ): site.addzero.kcloud.plugins.mcuconsole.modbus.device.McuModbusIndicatorLightsResponse

}