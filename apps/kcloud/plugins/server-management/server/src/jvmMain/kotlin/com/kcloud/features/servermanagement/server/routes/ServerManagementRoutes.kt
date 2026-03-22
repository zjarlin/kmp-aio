package com.kcloud.features.servermanagement.server.routes

import com.kcloud.model.ServerConfig
import com.kcloud.features.servermanagement.ServerManagementService
import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody

@GetMapping("/api/servers")
fun listServers(): Any {
    return serverManagementService().listServers()
}

@GetMapping("/api/servers/{id}")
fun readServer(
    @PathVariable id: String,
): Any {
    if (id.isBlank()) {
        throw IllegalArgumentException("缺少服务器 ID")
    }

    val server = serverManagementService().findServer(id)
    if (server == null) {
        throw NoSuchElementException("未找到服务器")
    }

    return server
}

@PutMapping("/api/servers")
fun saveServer(
    @RequestBody request: ServerConfig,
): Any {
    val result = serverManagementService().saveServer(request)
    if (!result.success) {
        throw IllegalArgumentException(result.message)
    }
    return result
}

@DeleteMapping("/api/servers/{id}")
fun deleteServer(
    @PathVariable id: String,
): Any {
    if (id.isBlank()) {
        throw IllegalArgumentException("缺少服务器 ID")
    }

    val result = serverManagementService().deleteServer(id)
    if (!result.success) {
        throw NoSuchElementException(result.message)
    }
    return result
}

private fun serverManagementService(): ServerManagementService {
    return KoinPlatform.getKoin().get()
}
