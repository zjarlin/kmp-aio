package com.kcloud.features.dotfiles.server.routes

import com.kcloud.features.dotfiles.DotfilesService
import com.kcloud.features.dotfiles.DotfilesSettings
import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody

@GetMapping("/api/dotfiles/settings")
fun readDotfilesSettings(): DotfilesSettings {
    return dotfilesService().loadSettings()
}

@PutMapping("/api/dotfiles/settings")
fun updateDotfilesSettings(
    @RequestBody request: DotfilesSettings,
): DotfilesSettings {
    return dotfilesService().saveSettings(request)
}

@GetMapping("/api/dotfiles/status")
fun readDotfilesStatus(): Any {
    return dotfilesService().readStatus()
}

@PostMapping("/api/dotfiles/init")
fun initializeDotfiles(): Any {
    return dotfilesService().initializeRepository()
}

@GetMapping("/api/dotfiles/diff")
fun readDotfilesDiff(): Any {
    return dotfilesService().diff()
}

@PostMapping("/api/dotfiles/apply")
fun applyDotfiles(): Any {
    return dotfilesService().applyChanges()
}

private fun dotfilesService(): DotfilesService {
    return KoinPlatform.getKoin().get()
}
