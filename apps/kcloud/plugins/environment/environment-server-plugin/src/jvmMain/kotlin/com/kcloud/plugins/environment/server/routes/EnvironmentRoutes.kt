package com.kcloud.plugins.environment.server.routes

import com.kcloud.plugins.environment.EnvironmentSetupService
import com.kcloud.plugins.environment.EnvironmentSetupSettings
import io.ktor.server.application.ApplicationCall
import org.koin.ktor.ext.getKoin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody

@PutMapping("/api/environment/settings")
fun saveEnvironmentSettings(
    call: ApplicationCall,
    @RequestBody request: EnvironmentSetupSettings,
): Any {
    return call.environmentSetupService().saveSettings(request)
}

@PostMapping("/api/environment/inspect")
fun inspectEnvironment(
    call: ApplicationCall,
    @RequestBody request: EnvironmentSetupSettings,
): Any {
    return call.environmentSetupService().inspectEnvironment(request)
}

@PostMapping("/api/environment/preview")
fun previewEnvironmentInstall(
    call: ApplicationCall,
    @RequestBody request: EnvironmentSetupSettings,
): Any {
    return call.environmentSetupService().previewInstall(request)
}

@PostMapping("/api/environment/install")
fun installEnvironment(
    call: ApplicationCall,
    @RequestBody request: EnvironmentSetupSettings,
): Any {
    return call.environmentSetupService().install(request)
}

private fun ApplicationCall.environmentSetupService(): EnvironmentSetupService {
    return application.getKoin().get()
}
