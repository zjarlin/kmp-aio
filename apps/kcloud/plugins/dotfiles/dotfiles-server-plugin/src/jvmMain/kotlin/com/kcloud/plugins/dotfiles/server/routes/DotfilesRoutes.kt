package com.kcloud.plugins.dotfiles.server.routes

import com.kcloud.plugins.dotfiles.DotfilesService
import com.kcloud.plugins.dotfiles.DotfilesSettings
import io.ktor.server.application.ApplicationCall
import org.koin.ktor.ext.getKoin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody

@GetMapping("/api/dotfiles/settings")
fun readDotfilesSettings(call: ApplicationCall): DotfilesSettings {
    return call.dotfilesService().loadSettings()
}

@PutMapping("/api/dotfiles/settings")
fun updateDotfilesSettings(
    call: ApplicationCall,
    @RequestBody request: DotfilesSettings,
): DotfilesSettings {
    return call.dotfilesService().saveSettings(request)
}

@GetMapping("/api/dotfiles/status")
fun readDotfilesStatus(call: ApplicationCall): Any {
    return call.dotfilesService().readStatus()
}

@PostMapping("/api/dotfiles/init")
fun initializeDotfiles(call: ApplicationCall): Any {
    return call.dotfilesService().initializeRepository()
}

@GetMapping("/api/dotfiles/diff")
fun readDotfilesDiff(call: ApplicationCall): Any {
    return call.dotfilesService().diff()
}

@PostMapping("/api/dotfiles/apply")
fun applyDotfiles(call: ApplicationCall): Any {
    return call.dotfilesService().applyChanges()
}

private fun ApplicationCall.dotfilesService(): DotfilesService {
    return application.getKoin().get()
}
