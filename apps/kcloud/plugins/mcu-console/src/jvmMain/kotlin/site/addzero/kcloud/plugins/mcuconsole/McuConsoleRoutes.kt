package site.addzero.kcloud.plugins.mcuconsole

import io.ktor.server.routing.Route
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import site.addzero.kcloud.plugins.mcuconsole.routes.closeMcuSession
import site.addzero.kcloud.plugins.mcuconsole.routes.executeMcuScript
import site.addzero.kcloud.plugins.mcuconsole.routes.getMcuFlashStatus
import site.addzero.kcloud.plugins.mcuconsole.routes.getMcuRuntimeStatus
import site.addzero.kcloud.plugins.mcuconsole.routes.getMcuScriptStatus
import site.addzero.kcloud.plugins.mcuconsole.routes.getMcuSession
import site.addzero.kcloud.plugins.mcuconsole.routes.listMcuRuntimeBundles
import site.addzero.kcloud.plugins.mcuconsole.routes.listMcuFlashProfiles
import site.addzero.kcloud.plugins.mcuconsole.routes.listMcuPorts
import site.addzero.kcloud.plugins.mcuconsole.routes.ensureMcuRuntime
import site.addzero.kcloud.plugins.mcuconsole.routes.openMcuSession
import site.addzero.kcloud.plugins.mcuconsole.routes.readMcuEvents
import site.addzero.kcloud.plugins.mcuconsole.routes.readMcuRecentLines
import site.addzero.kcloud.plugins.mcuconsole.routes.resetMcuSession
import site.addzero.kcloud.plugins.mcuconsole.routes.startMcuFlash
import site.addzero.kcloud.plugins.mcuconsole.routes.stopMcuScript
import site.addzero.kcloud.plugins.mcuconsole.routes.updateMcuSignals
import site.addzero.springktor.runtime.optionalRequestParam
import site.addzero.springktor.runtime.requireRequestBody

/**
 * 统一挂载 mcu-console 插件的后端路由。
 */
fun Route.mcuConsoleRoutes() {
    get("/api/mcu/ports") {
        call.respond(listMcuPorts())
    }
    get("/api/mcu/session") {
        call.respond(getMcuSession())
    }
    post("/api/mcu/session/open") {
        call.respond(openMcuSession(call.requireRequestBody()))
    }
    post("/api/mcu/session/close") {
        call.respond(closeMcuSession())
    }
    post("/api/mcu/session/reset") {
        call.respond(resetMcuSession(call.requireRequestBody()))
    }
    post("/api/mcu/session/signals") {
        call.respond(updateMcuSignals(call.requireRequestBody()))
    }
    post("/api/mcu/session/lines") {
        call.respond(readMcuRecentLines(call.requireRequestBody()))
    }
    get("/api/mcu/events") {
        call.respond(readMcuEvents(call.optionalRequestParam("afterSeq")))
    }
    post("/api/mcu/script/execute") {
        call.respond(executeMcuScript(call.requireRequestBody()))
    }
    post("/api/mcu/script/stop") {
        call.respond(stopMcuScript(call.requireRequestBody()))
    }
    get("/api/mcu/script/status") {
        call.respond(getMcuScriptStatus())
    }
    get("/api/mcu/flash/profiles") {
        call.respond(listMcuFlashProfiles())
    }
    post("/api/mcu/flash/start") {
        call.respond(startMcuFlash(call.requireRequestBody()))
    }
    get("/api/mcu/flash/status") {
        call.respond(getMcuFlashStatus())
    }
    get("/api/mcu/runtime/bundles") {
        call.respond(listMcuRuntimeBundles())
    }
    post("/api/mcu/runtime/ensure") {
        call.respond(ensureMcuRuntime(call.requireRequestBody()))
    }
    get("/api/mcu/runtime/status") {
        call.respond(getMcuRuntimeStatus())
    }
}
