package site.addzero.kcloud.plugins.mcuconsole.routes

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import site.addzero.kcloud.plugins.mcuconsole.McuRuntimeBundlesResponse
import site.addzero.kcloud.plugins.mcuconsole.McuRuntimeEnsureRequest
import site.addzero.kcloud.plugins.mcuconsole.McuRuntimeStatusResponse
import site.addzero.kcloud.plugins.mcuconsole.service.McuRuntimeEnsureService

/**
 * MCU 运行时服务端路由定义，同时作为客户端 API 生成源。
 */
@GetMapping("/api/mcu/runtime/bundles")
fun listMcuRuntimeBundles(): McuRuntimeBundlesResponse {
    return runtimeService().listBundles()
}

/**
 * 确保指定运行时已准备完成。
 */
@PostMapping("/api/mcu/runtime/ensure")
suspend fun ensureMcuRuntime(
    @RequestBody request: McuRuntimeEnsureRequest,
): McuRuntimeStatusResponse {
    return runtimeService().ensureRuntime(request)
}

/**
 * 读取当前运行时状态。
 */
@GetMapping("/api/mcu/runtime/status")
fun getMcuRuntimeStatus(): McuRuntimeStatusResponse {
    return runtimeService().getStatus()
}

private fun runtimeService(): McuRuntimeEnsureService {
    return KoinPlatform.getKoin().get()
}
