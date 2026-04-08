package site.addzero.kcloud.plugins.mcuconsole.routes

import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.mp.KoinPlatform
import site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashRequest
import site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashResetRequest

fun Route.registerMcuFlashRoutes() {
    val controller = KoinPlatform.getKoin().get<FlashController>()

    route("/api/mcu/flash") {
        get("/profiles") {
            call.respond(controller.listProfiles())
        }
        get("/probes") {
            call.respond(controller.listProbes())
        }
        post("/start") {
            val request = call.receive<McuFlashRequest>()
            call.respond(controller.startFlash(request))
        }
        get("/status") {
            call.respond(controller.getStatus())
        }
        post("/reset") {
            val request = call.receive<McuFlashResetRequest>()
            call.respond(controller.reset(request))
        }
    }
}
