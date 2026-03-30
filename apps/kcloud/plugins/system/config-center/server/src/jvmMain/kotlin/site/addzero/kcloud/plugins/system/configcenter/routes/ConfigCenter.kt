package site.addzero.kcloud.plugins.system.configcenter.routes

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import site.addzero.kcloud.plugins.system.configcenter.ConfigCenterService
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterValueDto
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterValueWriteRequest

@GetMapping("/api/system/config-center/value")
fun getConfigCenterValue(
    @RequestParam("namespace") namespace: String,
    @RequestParam("key") key: String,
    @RequestParam("active", required = false) active: String?,
): ConfigCenterValueDto {
    return service().readValue(
        namespace = namespace,
        key = key,
        active = active ?: "dev",
    )
}

@PutMapping("/api/system/config-center/value")
fun putConfigCenterValue(
    @RequestBody request: ConfigCenterValueWriteRequest,
): ConfigCenterValueDto {
    return service().writeValue(
        namespace = request.namespace,
        key = request.key,
        value = request.value,
        active = request.active,
    )
}

private fun service(): ConfigCenterService {
    return KoinPlatform.getKoin().get()
}
