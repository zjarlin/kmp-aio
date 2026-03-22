package com.kcloud.features.webdav.server.routes

import com.kcloud.features.webdav.WebDavConnectionConfig
import com.kcloud.features.webdav.WebDavPathRequest
import com.kcloud.features.webdav.WebDavWorkspaceService
import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@GetMapping("/api/webdav/settings")
fun readWebDavSettings(): WebDavConnectionConfig {
    return webDavWorkspaceService().loadSettings()
}

@PutMapping("/api/webdav/settings")
fun updateWebDavSettings(
    @RequestBody request: WebDavConnectionConfig,
): WebDavConnectionConfig {
    return webDavWorkspaceService().saveSettings(request)
}

@PostMapping("/api/webdav/test")
fun testWebDavConnection(
    @RequestBody request: WebDavConnectionConfig,
): Any {
    return webDavWorkspaceService().testConnection(request)
}

@GetMapping("/api/webdav/files")
fun listWebDavFiles(
    @RequestParam("path") path: String?,
): Any {
    return webDavWorkspaceService().listDirectory(path.orEmpty())
}

@PostMapping("/api/webdav/mkdir")
fun createWebDavDirectory(
    @RequestBody request: WebDavPathRequest,
): Any {
    return webDavWorkspaceService().createDirectory(request.path)
}

@PostMapping("/api/webdav/delete")
fun deleteWebDavPath(
    @RequestBody request: WebDavPathRequest,
): Any {
    return webDavWorkspaceService().deletePath(request.path)
}

private fun webDavWorkspaceService(): WebDavWorkspaceService {
    return KoinPlatform.getKoin().get()
}
