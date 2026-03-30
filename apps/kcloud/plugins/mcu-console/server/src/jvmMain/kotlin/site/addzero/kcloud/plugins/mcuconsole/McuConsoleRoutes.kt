package site.addzero.kcloud.plugins.mcuconsole

import io.ktor.server.response.*
import io.ktor.server.routing.*
import site.addzero.esp32_host_computer.generated.modbus.rtu.registerGeneratedModbusRtuRoutes
import site.addzero.kcloud.plugins.mcuconsole.routes.*
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
    registerGeneratedModbusRtuRoutes()
}
