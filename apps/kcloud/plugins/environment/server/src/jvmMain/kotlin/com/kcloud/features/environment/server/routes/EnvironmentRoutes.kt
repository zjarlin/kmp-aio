package com.kcloud.features.environment.server.routes

import com.kcloud.features.environment.EnvironmentSetupService
import com.kcloud.features.environment.EnvironmentSetupSettings
import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody

@PutMapping("/api/environment/settings")
fun saveEnvironmentSettings(
    @RequestBody request: EnvironmentSetupSettings,
): Any {
    return environmentSetupService().saveSettings(request)
}

@PostMapping("/api/environment/inspect")
fun inspectEnvironment(
    @RequestBody request: EnvironmentSetupSettings,
): Any {
    return environmentSetupService().inspectEnvironment(request)
}

@PostMapping("/api/environment/preview")
fun previewEnvironmentInstall(
    @RequestBody request: EnvironmentSetupSettings,
): Any {
    return environmentSetupService().previewInstall(request)
}

@PostMapping("/api/environment/install")
fun installEnvironment(
    @RequestBody request: EnvironmentSetupSettings,
): Any {
    return environmentSetupService().install(request)
}

private fun environmentSetupService(): EnvironmentSetupService {
    return KoinPlatform.getKoin().get()
}
