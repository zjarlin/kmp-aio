package site.addzero.kcloud.plugins.mcuconsole.api.external

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusCommandResponse
import site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusGpioWriteRequest
import site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusGpioModeRequest
import site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusPwmDutyRequest
import site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusServoAngleRequest

/**
 * 原始文件: site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusAtomicRoutes.kt
 * 基础路径: 
 */
interface McuModbusAtomicRoutesApi {

/**
 * writeMcuModbusGpio
 * HTTP方法: POST
 * 路径: /api/mcu/modbus/rtu/automic-modbus/gpio-write
 * 参数:
 *   - request: site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusGpioWriteRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusCommandResponse
 */
    @POST("/api/mcu/modbus/rtu/automic-modbus/gpio-write")
    @Headers("Content-Type: application/json")
    suspend fun writeMcuModbusGpio(
        @Body request: site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusGpioWriteRequest
    ): site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusCommandResponse

/**
 * writeMcuModbusGpioMode
 * HTTP方法: POST
 * 路径: /api/mcu/modbus/rtu/automic-modbus/gpio-mode
 * 参数:
 *   - request: site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusGpioModeRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusCommandResponse
 */
    @POST("/api/mcu/modbus/rtu/automic-modbus/gpio-mode")
    @Headers("Content-Type: application/json")
    suspend fun writeMcuModbusGpioMode(
        @Body request: site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusGpioModeRequest
    ): site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusCommandResponse

/**
 * writeMcuModbusPwmDuty
 * HTTP方法: POST
 * 路径: /api/mcu/modbus/rtu/automic-modbus/pwm-duty
 * 参数:
 *   - request: site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusPwmDutyRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusCommandResponse
 */
    @POST("/api/mcu/modbus/rtu/automic-modbus/pwm-duty")
    @Headers("Content-Type: application/json")
    suspend fun writeMcuModbusPwmDuty(
        @Body request: site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusPwmDutyRequest
    ): site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusCommandResponse

/**
 * writeMcuModbusServoAngle
 * HTTP方法: POST
 * 路径: /api/mcu/modbus/rtu/automic-modbus/servo-angle
 * 参数:
 *   - request: site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusServoAngleRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusCommandResponse
 */
    @POST("/api/mcu/modbus/rtu/automic-modbus/servo-angle")
    @Headers("Content-Type: application/json")
    suspend fun writeMcuModbusServoAngle(
        @Body request: site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusServoAngleRequest
    ): site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusCommandResponse

}