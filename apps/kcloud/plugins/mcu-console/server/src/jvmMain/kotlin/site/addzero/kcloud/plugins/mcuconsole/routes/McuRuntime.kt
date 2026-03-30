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
/**
 * 前端按钮: 烧录页“刷新资源”、控制台“确保运行时”、在线开发页“确保运行时”。
 * 作用: 列出运行时包与默认能力包定义。
 */
@GetMapping("/api/mcu/runtime/bundles")
fun listMcuRuntimeBundles(): McuRuntimeBundlesResponse {
    return runtimeService().listBundles()
}

/**
 * 前端按钮: 烧录页“刷内置运行时”、控制台“确保运行时”、在线开发页“确保运行时”。
 * 作用: 探测或刷写指定运行时，并更新运行时状态。
 */
@PostMapping("/api/mcu/runtime/ensure")
suspend fun ensureMcuRuntime(
    @RequestBody request: McuRuntimeEnsureRequest,
): McuRuntimeStatusResponse {
    return runtimeService().ensureRuntime(request)
}

/**
 * 前端按钮: 烧录页“刷新状态”、在线开发页“刷新”、调试页“刷新”。
 * 作用: 读取当前运行时状态。
 */
@GetMapping("/api/mcu/runtime/status")
fun getMcuRuntimeStatus(): McuRuntimeStatusResponse {
    return runtimeService().getStatus()
}

private fun runtimeService(): McuRuntimeEnsureService {
    return KoinPlatform.getKoin().get()
}
