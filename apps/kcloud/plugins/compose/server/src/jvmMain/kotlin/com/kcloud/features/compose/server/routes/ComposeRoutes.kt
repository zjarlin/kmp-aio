package com.kcloud.features.compose.server.routes

import com.kcloud.features.compose.ComposeCommandResult
import com.kcloud.features.compose.ComposeLogsResult
import com.kcloud.features.compose.ComposeManagerService
import com.kcloud.features.compose.ComposeManagerSettings
import com.kcloud.features.compose.ComposeRuntimeInfo
import com.kcloud.features.compose.ComposeServerTarget
import com.kcloud.features.compose.ComposeStackDraft
import com.kcloud.features.compose.ComposeStackSummary
import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@GetMapping("/api/compose/settings")
fun readComposeSettings(): ComposeManagerSettings {
    return composeManagerService().loadSettings()
}

@PutMapping("/api/compose/settings")
fun updateComposeSettings(
    @RequestBody request: ComposeManagerSettings
): ComposeManagerSettings {
    return composeManagerService().saveSettings(request)
}

@GetMapping("/api/compose/servers")
fun listComposeServers(): List<ComposeServerTarget> {
    return composeManagerService().listServerTargets()
}

@GetMapping("/api/compose/runtime")
suspend fun inspectComposeRuntime(): ComposeRuntimeInfo {
    return composeManagerService().inspectRuntime()
}

@GetMapping("/api/compose/stacks")
suspend fun listComposeStacks(): List<ComposeStackSummary> {
    return composeManagerService().listStacks()
}

@GetMapping("/api/compose/stacks/{name}")
suspend fun readComposeStack(
    @PathVariable name: String
): ComposeStackDraft {
    return composeManagerService().readStack(name)
        ?: throw NoSuchElementException("Compose stack not found: $name")
}

@PostMapping("/api/compose/stacks/validate")
suspend fun validateComposeDraft(
    @RequestBody request: ComposeStackDraft
): ComposeCommandResult {
    return composeManagerService().validateDraft(request)
}

@PutMapping("/api/compose/stacks/{name}")
suspend fun saveComposeStack(
    @PathVariable name: String,
    @RequestBody request: ComposeStackDraft
): ComposeCommandResult {
    return composeManagerService().saveStack(request.copy(name = name))
}

@PostMapping("/api/compose/stacks/{name}/up")
suspend fun upComposeStack(
    @PathVariable name: String
): ComposeCommandResult {
    return composeManagerService().upStack(name)
}

@PostMapping("/api/compose/stacks/{name}/down")
suspend fun downComposeStack(
    @PathVariable name: String
): ComposeCommandResult {
    return composeManagerService().downStack(name)
}

@PostMapping("/api/compose/stacks/{name}/restart")
suspend fun restartComposeStack(
    @PathVariable name: String
): ComposeCommandResult {
    return composeManagerService().restartStack(name)
}

@PostMapping("/api/compose/stacks/{name}/pull")
suspend fun pullComposeStack(
    @PathVariable name: String
): ComposeCommandResult {
    return composeManagerService().pullStack(name)
}

@GetMapping("/api/compose/stacks/{name}/logs")
suspend fun readComposeLogs(
    @PathVariable name: String,
    @RequestParam("tail") tail: Int?
): ComposeLogsResult {
    return composeManagerService().readLogs(name, tail ?: 200)
}

@DeleteMapping("/api/compose/stacks/{name}")
suspend fun deleteComposeStack(
    @PathVariable name: String
): ComposeCommandResult {
    return composeManagerService().deleteStack(name)
}

private fun composeManagerService(): ComposeManagerService {
    return KoinPlatform.getKoin().get()
}
