package site.addzero.kcloud.plugins.mcuconsole.routes

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import site.addzero.kcloud.plugins.mcuconsole.McuDeviceInfoPollRequest
import site.addzero.kcloud.plugins.mcuconsole.McuDeviceInfoResponse
import site.addzero.kcloud.plugins.mcuconsole.service.McuDeviceInfoService

/**
 * MCU 设备基础信息路由，同时作为客户端 API 生成源。
 */

/**
 * 前端控件: 设备信息卡片首屏加载、轮询失败后的状态回显。
 * 作用: 读取最近一次设备信息轮询结果。
 */
@GetMapping("/api/mcu/device-info")
fun getMcuDeviceInfo(): McuDeviceInfoResponse {
    return deviceInfoService().getStatus()
}

/**
 * 前端按钮: 设备信息卡片“刷新”、工作台定时轮询。
 * 作用: 主动向当前 MCU 发送设备信息查询命令。
 */
@PostMapping("/api/mcu/device-info/poll")
suspend fun pollMcuDeviceInfo(
    @RequestBody request: McuDeviceInfoPollRequest,
): McuDeviceInfoResponse {
    return deviceInfoService().pollDeviceInfo(request)
}

/**
 * 从 Koin 里解析设备信息服务。
 */
private fun deviceInfoService(): McuDeviceInfoService {
    return KoinPlatform.getKoin().get()
}
