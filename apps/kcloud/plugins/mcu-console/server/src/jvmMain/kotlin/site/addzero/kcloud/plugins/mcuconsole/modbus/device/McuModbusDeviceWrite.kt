package site.addzero.kcloud.plugins.mcuconsole.modbus.device

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

/**
 * MCU Modbus 写侧设备路由，同时作为客户端 API 生成源。
 */

/**
 * 前端按钮: Modbus 页“设置故障灯/运行灯”。
 * 作用: 通过当前已打开的串口会话写入故障灯和运行灯状态。
 */
@PostMapping("/api/mcu/modbus/indicator-lights")
suspend fun writeMcuModbusIndicatorLights(
    @RequestBody request: McuModbusIndicatorLightsRequest,
): McuModbusIndicatorLightsResponse {
    return deviceModbusService().writeIndicatorLights(
        faultLightOn = request.faultLightOn,
        runLightOn = request.runLightOn,
    )
}

private fun deviceModbusService(): DeviceModbusService {
    return KoinPlatform.getKoin().get()
}
