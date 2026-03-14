package com.kcloud.plugins.dotfiles.server.routes

import com.kcloud.plugins.dotfiles.DotfilesService
import com.kcloud.plugins.dotfiles.DotfilesSettings
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Routing.installDotfilesRoutes(
    service: DotfilesService
) {
    route("/api/dotfiles") {
        get("/settings") {
            call.respond(service.loadSettings())
        }

        put("/settings") {
            val request = call.receive<DotfilesSettings>()
            call.respond(service.saveSettings(request))
        }

        get("/status") {
            call.respond(service.readStatus())
        }

        post("/init") {
            call.respond(service.initializeRepository())
        }

        get("/diff") {
            call.respond(service.diff())
        }

        post("/apply") {
            call.respond(service.applyChanges())
        }
    }
}
