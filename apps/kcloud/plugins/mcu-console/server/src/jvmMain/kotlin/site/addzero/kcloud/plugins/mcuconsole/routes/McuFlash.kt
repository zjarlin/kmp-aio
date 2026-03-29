package site.addzero.kcloud.plugins.mcuconsole.routes

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import site.addzero.kcloud.plugins.mcuconsole.McuFlashProfilesResponse
import site.addzero.kcloud.plugins.mcuconsole.McuFlashRequest
import site.addzero.kcloud.plugins.mcuconsole.McuFlashStatusResponse
import site.addzero.kcloud.plugins.mcuconsole.service.McuFlashService

/**
 * MCU 烧录服务端路由定义，同时作为客户端 API 生成源。
 */
@GetMapping("/api/mcu/flash/profiles")
fun listMcuFlashProfiles(): McuFlashProfilesResponse {
    return flashService().listProfiles()
}

@PostMapping("/api/mcu/flash/start")
suspend fun startMcuFlash(
    @RequestBody request: McuFlashRequest,
): McuFlashStatusResponse {
    return flashService().flash(request)
}

/**
 * 读取最近一次烧录任务状态。
 */
@GetMapping("/api/mcu/flash/status")
fun getMcuFlashStatus(): McuFlashStatusResponse {
    return flashService().getStatus()
}

private fun flashService(): McuFlashService {
    return KoinPlatform.getKoin().get()
}
