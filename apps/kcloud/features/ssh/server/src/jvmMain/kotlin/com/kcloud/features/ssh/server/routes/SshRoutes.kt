package com.kcloud.features.ssh.server.routes

import com.kcloud.features.ssh.RemotePathRequest
import com.kcloud.features.ssh.SshConnectionConfig
import com.kcloud.features.ssh.SshWorkspaceService
import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@GetMapping("/api/ssh/settings")
fun readSshSettings(): SshConnectionConfig {
    return sshWorkspaceService().loadSettings()
}

@PutMapping("/api/ssh/settings")
fun updateSshSettings(
    @RequestBody request: SshConnectionConfig,
): SshConnectionConfig {
    return sshWorkspaceService().saveSettings(request)
}

@PostMapping("/api/ssh/test")
fun testSshConnection(
    @RequestBody request: SshConnectionConfig,
): Any {
    return sshWorkspaceService().testConnection(request)
}

@GetMapping("/api/ssh/files")
fun listSshFiles(
    @RequestParam("path") path: String?,
): Any {
    return sshWorkspaceService().listDirectory(path.orEmpty())
}

@PostMapping("/api/ssh/mkdir")
fun createSshDirectory(
    @RequestBody request: RemotePathRequest,
): Any {
    return sshWorkspaceService().createDirectory(request.path)
}

@PostMapping("/api/ssh/delete")
fun deleteSshPath(
    @RequestBody request: RemotePathRequest,
): Any {
    return sshWorkspaceService().deletePath(request.path)
}

private fun sshWorkspaceService(): SshWorkspaceService {
    return KoinPlatform.getKoin().get()
}
