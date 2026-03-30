package site.addzero.kcloud.plugins.mcuconsole.routes

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import site.addzero.kcloud.plugins.mcuconsole.McuFlashDownloadRequest
import site.addzero.kcloud.plugins.mcuconsole.McuFlashDownloadResponse
import site.addzero.kcloud.plugins.mcuconsole.McuFlashProfilesResponse
import site.addzero.kcloud.plugins.mcuconsole.McuFlashRequest
import site.addzero.kcloud.plugins.mcuconsole.McuFlashStatusResponse
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
 * 前端按钮: 烧录页“开始烧录”、烧录页“在线下载并烧录”。
 * 作用: 按当前能力包配置执行烧录。
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
 * 前端按钮: 烧录页“在线下载”、烧录页“在线下载并烧录”。
 * 作用: 在线下载固件并缓存到本机，供后续烧录复用。
 */
@PostMapping("/api/mcu/flash/download")
suspend fun downloadMcuFlashFirmware(
    @RequestBody request: McuFlashDownloadRequest,
): McuFlashDownloadResponse {
    return flashService().downloadFirmware(request)
}

private fun flashService(): McuFlashService {
    return KoinPlatform.getKoin().get()
}
