package com.kcloud.plugins.webdav.server.routes

import com.kcloud.plugins.webdav.WebDavConnectionConfig
import com.kcloud.plugins.webdav.WebDavPathRequest
import com.kcloud.plugins.webdav.WebDavWorkspaceService
import io.ktor.server.application.ApplicationCall
import org.koin.ktor.ext.getKoin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@GetMapping("/api/webdav/settings")
fun readWebDavSettings(call: ApplicationCall): WebDavConnectionConfig {
    return call.webDavWorkspaceService().loadSettings()
}

@PutMapping("/api/webdav/settings")
fun updateWebDavSettings(
    call: ApplicationCall,
    @RequestBody request: WebDavConnectionConfig,
): WebDavConnectionConfig {
    return call.webDavWorkspaceService().saveSettings(request)
}

@PostMapping("/api/webdav/test")
fun testWebDavConnection(
    call: ApplicationCall,
    @RequestBody request: WebDavConnectionConfig,
): Any {
    return call.webDavWorkspaceService().testConnection(request)
}

@GetMapping("/api/webdav/files")
fun listWebDavFiles(
    call: ApplicationCall,
    @RequestParam("path") path: String?,
): Any {
    return call.webDavWorkspaceService().listDirectory(path.orEmpty())
}

@PostMapping("/api/webdav/mkdir")
fun createWebDavDirectory(
    call: ApplicationCall,
    @RequestBody request: WebDavPathRequest,
): Any {
    return call.webDavWorkspaceService().createDirectory(request.path)
}

@PostMapping("/api/webdav/delete")
fun deleteWebDavPath(
    call: ApplicationCall,
    @RequestBody request: WebDavPathRequest,
): Any {
    return call.webDavWorkspaceService().deletePath(request.path)
}

private fun ApplicationCall.webDavWorkspaceService(): WebDavWorkspaceService {
    return application.getKoin().get()
}
