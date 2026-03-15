package com.kcloud.plugins.ssh.server.routes

import com.kcloud.plugins.ssh.RemotePathRequest
import com.kcloud.plugins.ssh.SshConnectionConfig
import com.kcloud.plugins.ssh.SshWorkspaceService
import io.ktor.server.application.ApplicationCall
import org.koin.ktor.ext.getKoin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@GetMapping("/api/ssh/settings")
fun readSshSettings(call: ApplicationCall): SshConnectionConfig {
    return call.sshWorkspaceService().loadSettings()
}

@PutMapping("/api/ssh/settings")
fun updateSshSettings(
    call: ApplicationCall,
    @RequestBody request: SshConnectionConfig,
): SshConnectionConfig {
    return call.sshWorkspaceService().saveSettings(request)
}

@PostMapping("/api/ssh/test")
fun testSshConnection(
    call: ApplicationCall,
    @RequestBody request: SshConnectionConfig,
): Any {
    return call.sshWorkspaceService().testConnection(request)
}

@GetMapping("/api/ssh/files")
fun listSshFiles(
    call: ApplicationCall,
    @RequestParam("path") path: String?,
): Any {
    return call.sshWorkspaceService().listDirectory(path.orEmpty())
}

@PostMapping("/api/ssh/mkdir")
fun createSshDirectory(
    call: ApplicationCall,
    @RequestBody request: RemotePathRequest,
): Any {
    return call.sshWorkspaceService().createDirectory(request.path)
}

@PostMapping("/api/ssh/delete")
fun deleteSshPath(
    call: ApplicationCall,
    @RequestBody request: RemotePathRequest,
): Any {
    return call.sshWorkspaceService().deletePath(request.path)
}

private fun ApplicationCall.sshWorkspaceService(): SshWorkspaceService {
    return application.getKoin().get()
}
