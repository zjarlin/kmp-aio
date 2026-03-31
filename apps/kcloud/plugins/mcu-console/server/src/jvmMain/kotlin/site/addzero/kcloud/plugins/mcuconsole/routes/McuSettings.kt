package site.addzero.kcloud.plugins.mcuconsole.routes

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import site.addzero.kcloud.plugins.mcuconsole.McuDeviceProfileIso
import site.addzero.kcloud.plugins.mcuconsole.McuTransportProfileIso
import site.addzero.kcloud.plugins.mcuconsole.McuTransportProfilesResponse
import site.addzero.kcloud.plugins.mcuconsole.service.McuConsoleSettingsService

/**
 * MCU 控制台设置相关路由，同时作为客户端 API 生成源。
 */
/**
 * 前端控件: 控制台/Modbus 左侧串口列表选中后的备注输入框自动回填。
 * 作用: 按设备键读取已保存的串口备注与设备画像。
 */
@GetMapping("/api/mcu/device-profile")
fun getMcuDeviceProfile(
    @RequestParam("deviceKey") deviceKey: String?,
): McuDeviceProfileIso {
    return settingsService().getDeviceProfile(deviceKey)
}

/**
 * 前端按钮: 控制台串口备注“保存当前串口备注”。
 * 作用: 保存当前串口设备的备注、厂商和识别键映射。
 */
@PostMapping("/api/mcu/device-profile")
fun saveMcuDeviceProfile(
    @RequestBody request: McuDeviceProfileIso,
): McuDeviceProfileIso {
    return settingsService().saveDeviceProfile(request)
}

/**
 * 前端控件: 控制台/Modbus “已保存连接”列表。
 * 作用: 读取串口自动发现页保存的连接草稿，以及 Modbus RTU 页复用的串口参数草稿。
 */
@GetMapping("/api/mcu/transport-profiles")
fun listMcuTransportProfiles(): McuTransportProfilesResponse {
    return McuTransportProfilesResponse(
        items = settingsService().listTransportProfiles(),
    )
}

/**
 * 前端按钮: 控制台“保存连接”、Modbus“保存连接”。
 * 作用: 保存当前编辑中的连接草稿。
 */
@PostMapping("/api/mcu/transport-profile")
fun saveMcuTransportProfile(
    @RequestBody request: McuTransportProfileIso,
): McuTransportProfileIso {
    return settingsService().saveTransportProfile(request)
}

/**
 * 前端按钮: 控制台/Modbus “已保存连接”卡片里的“删除”。
 * 作用: 删除指定连接配置。
 */
@PostMapping("/api/mcu/transport-profile/delete")
fun deleteMcuTransportProfile(
    @RequestParam("profileKey") profileKey: String,
): McuTransportProfilesResponse {
    return McuTransportProfilesResponse(
        items = settingsService().deleteTransportProfile(profileKey),
    )
}

private fun settingsService(): McuConsoleSettingsService {
    return KoinPlatform.getKoin().get()
}
