package com.kcloud.server.routes

import com.kcloud.model.ServerConfig
import com.kcloud.plugins.servermanagement.ServerManagementService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Routing.installServerManagementRoutes(
    service: ServerManagementService
) {
    route("/api/servers") {
        get {
            call.respond(service.listServers())
        }

        get("/{id}") {
            val serverId = call.parameters["id"].orEmpty()
            if (serverId.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "缺少服务器 ID")
                return@get
            }

            val server = service.findServer(serverId)
            if (server == null) {
                call.respond(HttpStatusCode.NotFound, "未找到服务器")
                return@get
            }

            call.respond(server)
        }

        put {
            val request = call.receive<ServerConfig>()
            val result = service.saveServer(request)
            call.respond(
                if (result.success) {
                    HttpStatusCode.OK
                } else {
                    HttpStatusCode.BadRequest
                },
                result
            )
        }

        delete("/{id}") {
            val serverId = call.parameters["id"].orEmpty()
            if (serverId.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "缺少服务器 ID")
                return@delete
            }

            val result = service.deleteServer(serverId)
            call.respond(
                if (result.success) {
                    HttpStatusCode.OK
                } else {
                    HttpStatusCode.NotFound
                },
                result
            )
        }
    }
}
