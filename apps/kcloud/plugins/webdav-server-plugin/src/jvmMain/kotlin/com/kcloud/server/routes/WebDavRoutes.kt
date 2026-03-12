package com.kcloud.server.routes

import com.kcloud.plugins.webdav.WebDavConnectionConfig
import com.kcloud.plugins.webdav.WebDavPathRequest
import com.kcloud.plugins.webdav.WebDavWorkspaceService
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Routing.installWebDavRoutes(
    service: WebDavWorkspaceService
) {
    route("/api/webdav") {
        get("/settings") {
            call.respond(service.loadSettings())
        }

        put("/settings") {
            val request = call.receive<WebDavConnectionConfig>()
            call.respond(service.saveSettings(request))
        }

        post("/test") {
            val request = call.receive<WebDavConnectionConfig>()
            call.respond(service.testConnection(request))
        }

        get("/files") {
            val path = call.request.queryParameters["path"].orEmpty()
            call.respond(service.listDirectory(path))
        }

        post("/mkdir") {
            val request = call.receive<WebDavPathRequest>()
            call.respond(service.createDirectory(request.path))
        }

        post("/delete") {
            val request = call.receive<WebDavPathRequest>()
            call.respond(service.deletePath(request.path))
        }
    }
}
