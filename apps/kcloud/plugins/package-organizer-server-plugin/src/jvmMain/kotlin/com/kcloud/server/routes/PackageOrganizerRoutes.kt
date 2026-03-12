package com.kcloud.server.routes

import com.kcloud.plugins.packages.PackageOrganizerService
import com.kcloud.plugins.packages.PackageOrganizerSettings
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Routing.installPackageOrganizerRoutes(
    service: PackageOrganizerService
) {
    route("/api/packages") {
        get("/settings") {
            call.respond(service.loadSettings())
        }

        put("/settings") {
            val request = call.receive<PackageOrganizerSettings>()
            call.respond(service.saveSettings(request))
        }

        get {
            call.respond(service.scanPackages())
        }

        post("/organize") {
            call.respond(service.organizePackages())
        }
    }
}
