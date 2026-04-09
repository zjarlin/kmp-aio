package site.addzero.kcloud.plugins.mcuconsole.routes

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import site.addzero.kcloud.plugins.mcuconsole.modbus.device.DeviceApi
import site.addzero.kcloud.plugins.mcuconsole.modbus.device.Device24PowerLightsRegisters
import site.addzero.kcloud.plugins.mcuconsole.modbus.device.DeviceRuntimeInfoRegisters
import site.addzero.kcloud.plugins.mcuconsole.modbus.device.FlashConfigRegisters

/**
 * MCU 设备基础信息路由，同时作为客户端 API 生成源。
 */
@RestController
@RequestMapping("/device")
class DeviceInfoController(
    private val deviceApi: DeviceApi,
) {
    @GetMapping("/getDeviceInfo")
    suspend fun getDeviceInfo(): DeviceRuntimeInfoRegisters {
        return deviceApi.getDeviceInfo()
    }

    @GetMapping("/get24PowerLights")
    suspend fun get24PowerLights(): Device24PowerLightsRegisters {
        return deviceApi.get24PowerLights()
    }

    @GetMapping("/getFlashConfig")
    suspend fun getFlashConfig(): FlashConfigRegisters {
        return deviceApi.getFlashConfig()
    }
}
