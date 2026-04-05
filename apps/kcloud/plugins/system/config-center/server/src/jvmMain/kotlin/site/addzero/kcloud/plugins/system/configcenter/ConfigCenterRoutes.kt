package site.addzero.kcloud.plugins.system.configcenter

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import org.koin.mp.KoinPlatform
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterValueWriteRequest
import site.addzero.kcloud.plugins.system.configcenter.currentRuntimeConfigCenterActive
import site.addzero.kcloud.plugins.system.configcenter.spi.ConfigValueServiceSpi
import site.addzero.springktor.runtime.optionalRequestParam
import site.addzero.springktor.runtime.requireRequestBody
import site.addzero.springktor.runtime.requireRequestParam

fun Route.configCenterRoutes() {
    get("/api/system/config-center/value") {
        call.respond(
            configCenterService().readValue(
                namespace = call.requireRequestParam("namespace"),
                key = call.requireRequestParam("key"),
                active = call.optionalRequestParam<String>("active")
                    ?.trim()
                    ?.takeIf(String::isNotBlank)
                    ?: currentRuntimeConfigCenterActive(),
            ),
        )
    }
    put("/api/system/config-center/value") {
        val request = call.requireRequestBody<ConfigCenterValueWriteRequest>()
        call.respond(
            configCenterService().writeValue(
                namespace = request.namespace,
                key = request.key,
                value = request.value,
                active = request.active,
            ),
        )
    }
}

private fun configCenterService(): ConfigValueServiceSpi {
    return KoinPlatform.getKoin().get()
}
