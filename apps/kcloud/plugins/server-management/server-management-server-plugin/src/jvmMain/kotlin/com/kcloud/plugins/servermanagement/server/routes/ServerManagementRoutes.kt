package com.kcloud.plugins.servermanagement.server.routes

import com.kcloud.model.ServerConfig
import com.kcloud.plugins.servermanagement.ServerManagementService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import org.koin.ktor.ext.getKoin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody

@GetMapping("/api/servers")
fun listServers(call: ApplicationCall): Any {
    return call.serverManagementService().listServers()
}

@GetMapping("/api/servers/{id}")
fun readServer(
    call: ApplicationCall,
    @PathVariable id: String,
): Any {
    if (id.isBlank()) {
        call.response.status(HttpStatusCode.BadRequest)
        return "缺少服务器 ID"
    }

    val server = call.serverManagementService().findServer(id)
    if (server == null) {
        call.response.status(HttpStatusCode.NotFound)
        return "未找到服务器"
    }

    return server
}

@PutMapping("/api/servers")
fun saveServer(
    call: ApplicationCall,
    @RequestBody request: ServerConfig,
): Any {
    val result = call.serverManagementService().saveServer(request)
    if (!result.success) {
        call.response.status(HttpStatusCode.BadRequest)
    }
    return result
}

@DeleteMapping("/api/servers/{id}")
fun deleteServer(
    call: ApplicationCall,
    @PathVariable id: String,
): Any {
    if (id.isBlank()) {
        call.response.status(HttpStatusCode.BadRequest)
        return "缺少服务器 ID"
    }

    val result = call.serverManagementService().deleteServer(id)
    if (!result.success) {
        call.response.status(HttpStatusCode.NotFound)
    }
    return result
}

private fun ApplicationCall.serverManagementService(): ServerManagementService {
    return application.getKoin().get()
}
