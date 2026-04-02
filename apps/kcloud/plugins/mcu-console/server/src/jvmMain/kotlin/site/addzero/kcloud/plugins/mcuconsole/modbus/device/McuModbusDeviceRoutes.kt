package site.addzero.kcloud.plugins.mcuconsole.modbus.device

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping

/**
 * MCU Modbus 读设备状态路由，同时作为客户端 API 生成源。
 */

/**
 * 前端按钮: Modbus 页“读取 24 路电源灯”。
 * 作用: 通过当前已打开的串口会话读取 24 路电源灯状态。
 */
@GetMapping("/api/mcu/modbus/power-lights")
suspend fun getMcuModbusPowerLights(): McuModbusPowerLightsResponse {
    return deviceModbusService().get24PowerLights()
}

/**
 * 前端按钮: Modbus 页“读取设备信息”。
 * 作用: 通过当前已打开的串口会话读取板卡运行信息。
 */
@GetMapping("/api/mcu/modbus/device-info")
suspend fun getMcuModbusDeviceInfo(): McuModbusDeviceInfoResponse {
    return deviceModbusService().getDeviceInfo()
}

private fun deviceModbusService(): DeviceModbusService {
    return KoinPlatform.getKoin().get()
}
