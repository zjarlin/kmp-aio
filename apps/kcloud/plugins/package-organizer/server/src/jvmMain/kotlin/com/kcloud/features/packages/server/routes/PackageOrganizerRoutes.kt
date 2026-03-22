package com.kcloud.features.packages.server.routes

import com.kcloud.features.packages.PackageOrganizerService
import com.kcloud.features.packages.PackageOrganizerSettings
import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody

@GetMapping("/api/packages/settings")
fun readPackageSettings(): PackageOrganizerSettings {
    return packageOrganizerService().loadSettings()
}

@PutMapping("/api/packages/settings")
fun updatePackageSettings(
    @RequestBody request: PackageOrganizerSettings,
): PackageOrganizerSettings {
    return packageOrganizerService().saveSettings(request)
}

@GetMapping("/api/packages")
fun listPackages(): Any {
    return packageOrganizerService().scanPackages()
}

@PostMapping("/api/packages/organize")
fun organizePackages(): Any {
    return packageOrganizerService().organizePackages()
}

private fun packageOrganizerService(): PackageOrganizerService {
    return KoinPlatform.getKoin().get()
}
