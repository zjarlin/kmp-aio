package com.kcloud.plugins.ssh.server.routes

import com.kcloud.plugins.ssh.RemotePathRequest
import com.kcloud.plugins.ssh.SshConnectionConfig
import com.kcloud.plugins.ssh.SshWorkspaceService
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Routing.installSshRoutes(
    service: SshWorkspaceService
) {
    route("/api/ssh") {
        get("/settings") {
            call.respond(service.loadSettings())
        }

        put("/settings") {
            val request = call.receive<SshConnectionConfig>()
            call.respond(service.saveSettings(request))
        }

        post("/test") {
            val request = call.receive<SshConnectionConfig>()
            call.respond(service.testConnection(request))
        }

        get("/files") {
            val path = call.request.queryParameters["path"].orEmpty()
            call.respond(service.listDirectory(path))
        }

        post("/mkdir") {
            val request = call.receive<RemotePathRequest>()
            call.respond(service.createDirectory(request.path))
        }

        post("/delete") {
            val request = call.receive<RemotePathRequest>()
            call.respond(service.deletePath(request.path))
        }
    }
}
