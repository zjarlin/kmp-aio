package com.kcloud.plugins.packages.server.routes

import com.kcloud.plugins.packages.PackageOrganizerService
import com.kcloud.plugins.packages.PackageOrganizerSettings
import io.ktor.server.application.ApplicationCall
import org.koin.ktor.ext.getKoin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody

@GetMapping("/api/packages/settings")
fun readPackageSettings(call: ApplicationCall): PackageOrganizerSettings {
    return call.packageOrganizerService().loadSettings()
}

@PutMapping("/api/packages/settings")
fun updatePackageSettings(
    call: ApplicationCall,
    @RequestBody request: PackageOrganizerSettings,
): PackageOrganizerSettings {
    return call.packageOrganizerService().saveSettings(request)
}

@GetMapping("/api/packages")
fun listPackages(call: ApplicationCall): Any {
    return call.packageOrganizerService().scanPackages()
}

@PostMapping("/api/packages/organize")
fun organizePackages(call: ApplicationCall): Any {
    return call.packageOrganizerService().organizePackages()
}

private fun ApplicationCall.packageOrganizerService(): PackageOrganizerService {
    return application.getKoin().get()
}
