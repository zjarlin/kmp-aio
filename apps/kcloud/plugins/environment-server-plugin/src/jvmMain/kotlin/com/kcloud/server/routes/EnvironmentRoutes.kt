package com.kcloud.server.routes

import com.kcloud.plugins.environment.EnvironmentSetupService
import com.kcloud.plugins.environment.EnvironmentSetupSettings
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Routing.installEnvironmentRoutes(
    service: EnvironmentSetupService
) {
    route("/api/environment") {
        put("/settings") {
            val request = call.receive<EnvironmentSetupSettings>()
            call.respond(service.saveSettings(request))
        }

        post("/inspect") {
            val request = call.receive<EnvironmentSetupSettings>()
            call.respond(service.inspectEnvironment(request))
        }

        post("/preview") {
            val request = call.receive<EnvironmentSetupSettings>()
            call.respond(service.previewInstall(request))
        }

        post("/install") {
            val request = call.receive<EnvironmentSetupSettings>()
            call.respond(service.install(request))
        }
    }
}
