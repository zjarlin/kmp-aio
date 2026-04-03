package site.addzero.kcloud.plugins.mcuconsole.routes

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import site.addzero.kcloud.plugins.mcuconsole.McuFlashProbesResponse
import site.addzero.kcloud.plugins.mcuconsole.McuFlashProfilesResponse
import site.addzero.kcloud.plugins.mcuconsole.McuFlashRequest
import site.addzero.kcloud.plugins.mcuconsole.McuFlashStatusResponse
import site.addzero.kcloud.plugins.mcuconsole.McuResetRequest
import site.addzero.kcloud.plugins.mcuconsole.service.McuFlashService

/**
 * MCU 烧录服务端路由定义，同时作为客户端 API 生成源。
 */
/**
 * 前端按钮: 烧录页“刷新资源”。
 * 作用: 读取烧录菜单可选的能力包定义。
 */
@GetMapping("/api/mcu/flash/profiles")
fun listMcuFlashProfiles(): McuFlashProfilesResponse {
    return flashService().listProfiles()
}

/**
 * 前端按钮: 烧录页“刷新探针”。
 * 作用: 枚举当前可用的 ST-Link 调试探针。
 */
@GetMapping("/api/mcu/flash/probes")
fun listMcuFlashProbes(): McuFlashProbesResponse {
    return flashService().listProbes()
}

/**
 * 前端按钮: 烧录页“开始烧录”。
 * 作用: 通过 ST-Link + SWD 执行烧录。
 */
@PostMapping("/api/mcu/flash/start")
suspend fun startMcuFlash(
    @RequestBody request: McuFlashRequest,
): McuFlashStatusResponse {
    return flashService().flash(request)
}

/**
 * 前端按钮: 烧录页“刷新状态”。
 * 作用: 读取最近一次烧录任务状态。
 */
@GetMapping("/api/mcu/flash/status")
fun getMcuFlashStatus(): McuFlashStatusResponse {
    return flashService().getStatus()
}

/**
 * 前端按钮: 烧录页“复位设备”、控制台“设备复位”。
 * 作用: 通过 ST-Link 脉冲复位 NRST。
 */
@PostMapping("/api/mcu/flash/reset")
suspend fun resetMcuFlashTarget(
    @RequestBody request: McuResetRequest,
    @RequestParam("profileId") profileId: String?,
    @RequestParam("probeSerialNumber") probeSerialNumber: String?,
): McuFlashStatusResponse {
    return flashService().reset(
        request = request,
        profileId = profileId,
        probeSerialNumber = probeSerialNumber,
    )
}

private fun flashService(): McuFlashService {
    return KoinPlatform.getKoin().get()
}
