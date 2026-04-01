package site.addzero.kcloud.plugins.mcuconsole.api.external

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.plugins.mcuconsole.modbus.model.McuModbusPowerLightsResponse
import site.addzero.kcloud.plugins.mcuconsole.modbus.model.McuModbusDeviceInfoResponse

/**
 * 原始文件: site.addzero.kcloud.plugins.mcuconsole.routes.McuModbusDevice.kt
 * 基础路径: 
 */
interface McuModbusDeviceApi {

/**
 * getMcuModbusPowerLights
 * HTTP方法: GET
 * 路径: /api/mcu/modbus/power-lights
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.modbus.model.McuModbusPowerLightsResponse
 */
    @GET("/api/mcu/modbus/power-lights")
    suspend fun getMcuModbusPowerLights(): site.addzero.kcloud.plugins.mcuconsole.modbus.model.McuModbusPowerLightsResponse

/**
 * getMcuModbusDeviceInfo
 * HTTP方法: GET
 * 路径: /api/mcu/modbus/device-info
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.modbus.model.McuModbusDeviceInfoResponse
 */
    @GET("/api/mcu/modbus/device-info")
    suspend fun getMcuModbusDeviceInfo(): site.addzero.kcloud.plugins.mcuconsole.modbus.model.McuModbusDeviceInfoResponse

}