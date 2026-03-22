package site.addzero.remotecompose.server.routes

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import site.addzero.remotecompose.server.RemoteComposeSchemaService
import site.addzero.remotecompose.shared.RemoteComposeLocale
import site.addzero.remotecompose.shared.RemoteComposeScreenPayload
import site.addzero.remotecompose.shared.RemoteComposeScreenSummary

@GetMapping("/api/remote-compose/screens")
fun listRemoteComposeScreens(
    @RequestParam("locale") locale: String?,
): List<RemoteComposeScreenSummary> {
    return schemaService().listScreens(RemoteComposeLocale.fromCode(locale))
}

@GetMapping("/api/remote-compose/screens/{screenId}")
fun readRemoteComposeScreen(
    @PathVariable screenId: String,
    @RequestParam("locale") locale: String?,
): RemoteComposeScreenPayload {
    return schemaService().loadScreen(
        screenId = screenId,
        locale = RemoteComposeLocale.fromCode(locale),
    )
}

private fun schemaService(): RemoteComposeSchemaService {
    return KoinPlatform.getKoin().get()
}
