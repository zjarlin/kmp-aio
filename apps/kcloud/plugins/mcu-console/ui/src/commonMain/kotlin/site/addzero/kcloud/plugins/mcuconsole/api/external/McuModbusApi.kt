package site.addzero.kcloud.plugins.mcuconsole.api.external

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST
import site.addzero.kcloud.plugins.mcuconsole.McuModbusCommandResponse
import site.addzero.kcloud.plugins.mcuconsole.McuModbusGpioModeRequest
import site.addzero.kcloud.plugins.mcuconsole.McuModbusGpioWriteRequest
import site.addzero.kcloud.plugins.mcuconsole.McuModbusPwmDutyRequest
import site.addzero.kcloud.plugins.mcuconsole.McuModbusServoAngleRequest

interface McuModbusApi {
    @POST("/api/mcu/modbus/rtu/automic-modbus/gpio-write")
    suspend fun gpioWrite(
        @Body request: McuModbusGpioWriteRequest,
    ): McuModbusCommandResponse

    @POST("/api/mcu/modbus/rtu/automic-modbus/gpio-mode")
    suspend fun gpioMode(
        @Body request: McuModbusGpioModeRequest,
    ): McuModbusCommandResponse

    @POST("/api/mcu/modbus/rtu/automic-modbus/pwm-duty")
    suspend fun pwmDuty(
        @Body request: McuModbusPwmDutyRequest,
    ): McuModbusCommandResponse

    @POST("/api/mcu/modbus/rtu/automic-modbus/servo-angle")
    suspend fun servoAngle(
        @Body request: McuModbusServoAngleRequest,
    ): McuModbusCommandResponse
}
