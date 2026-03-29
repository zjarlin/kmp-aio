package site.addzero.kcloud.plugins.mcuconsole.routes

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import site.addzero.kcloud.plugins.mcuconsole.McuRuntimeBundlesResponse
import site.addzero.kcloud.plugins.mcuconsole.McuRuntimeEnsureRequest
import site.addzero.kcloud.plugins.mcuconsole.McuRuntimeStatusResponse
import site.addzero.kcloud.plugins.mcuconsole.service.McuRuntimeEnsureService

@GetMapping("/api/mcu/runtime/bundles")
fun listMcuRuntimeBundles(): McuRuntimeBundlesResponse {
    return runtimeService().listBundles()
}

@PostMapping("/api/mcu/runtime/ensure")
suspend fun ensureMcuRuntime(
    @RequestBody request: McuRuntimeEnsureRequest,
): McuRuntimeStatusResponse {
    return runtimeService().ensureRuntime(request)
}

@GetMapping("/api/mcu/runtime/status")
fun getMcuRuntimeStatus(): McuRuntimeStatusResponse {
    return runtimeService().getStatus()
}

private fun runtimeService(): McuRuntimeEnsureService {
    return KoinPlatform.getKoin().get()
}
