package site.addzero.kcloud.plugins.system.configcenter.routes

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.*
import site.addzero.kcloud.plugins.system.configcenter.ConfigCenterService
import site.addzero.kcloud.plugins.system.configcenter.api.*

@GetMapping("/api/system/config-center/projects")
fun listConfigCenterProjects(): List<ConfigCenterProjectDto> {
    return service().listProjects()
}

@PostMapping("/api/system/config-center/projects")
fun createConfigCenterProject(
    @RequestBody request: ConfigCenterProjectMutationRequest,
): ConfigCenterProjectDto {
    return service().createProject(request)
}

@PutMapping("/api/system/config-center/projects/{projectId}")
fun updateConfigCenterProject(
    @PathVariable("projectId") projectId: Long,
    @RequestBody request: ConfigCenterProjectMutationRequest,
): ConfigCenterProjectDto {
    return service().updateProject(projectId, request)
}

@GetMapping("/api/system/config-center/projects/{projectId}/environments")
fun listConfigCenterEnvironments(
    @PathVariable("projectId") projectId: Long,
): List<ConfigCenterEnvironmentDto> {
    return service().listEnvironments(projectId)
}

@PostMapping("/api/system/config-center/projects/{projectId}/environments")
fun createConfigCenterEnvironment(
    @PathVariable("projectId") projectId: Long,
    @RequestBody request: ConfigCenterEnvironmentMutationRequest,
): ConfigCenterEnvironmentDto {
    return service().createEnvironment(projectId, request)
}

@PutMapping("/api/system/config-center/projects/{projectId}/environments/{environmentId}")
fun updateConfigCenterEnvironment(
    @PathVariable("projectId") projectId: Long,
    @PathVariable("environmentId") environmentId: Long,
    @RequestBody request: ConfigCenterEnvironmentMutationRequest,
): ConfigCenterEnvironmentDto {
    return service().updateEnvironment(projectId, environmentId, request)
}

@GetMapping("/api/system/config-center/projects/{projectId}/configs")
fun listConfigCenterConfigs(
    @PathVariable("projectId") projectId: Long,
): List<ConfigCenterConfigDto> {
    return service().listConfigs(projectId)
}

@PostMapping("/api/system/config-center/projects/{projectId}/configs")
fun createConfigCenterConfig(
    @PathVariable("projectId") projectId: Long,
    @RequestBody request: ConfigCenterConfigMutationRequest,
): ConfigCenterConfigDto {
    return service().createConfig(projectId, request)
}

@PutMapping("/api/system/config-center/configs/{configId}")
fun updateConfigCenterConfig(
    @PathVariable("configId") configId: Long,
    @RequestBody request: ConfigCenterConfigMutationRequest,
): ConfigCenterConfigDto {
    return service().updateConfig(configId, request)
}

@GetMapping("/api/system/config-center/configs/{configId}/secrets")
fun listConfigCenterSecrets(
    @PathVariable("configId") configId: Long,
    @RequestParam("includeInherited", required = false) includeInherited: Boolean?,
): List<ConfigCenterSecretDto> {
    return service().listSecrets(
        configId = configId,
        includeInherited = includeInherited ?: true,
    )
}

@PostMapping("/api/system/config-center/secrets")
fun createConfigCenterSecret(
    @RequestBody request: ConfigCenterSecretMutationRequest,
): ConfigCenterSecretDto {
    return service().saveSecret(request)
}

@PutMapping("/api/system/config-center/secrets/{secretId}")
fun updateConfigCenterSecret(
    @PathVariable("secretId") secretId: Long,
    @RequestBody request: ConfigCenterSecretMutationRequest,
): ConfigCenterSecretDto {
    return service().saveSecret(request, secretId)
}

@DeleteMapping("/api/system/config-center/secrets/{secretId}")
fun deleteConfigCenterSecret(
    @PathVariable("secretId") secretId: Long,
) {
    service().deleteSecret(secretId)
}

@GetMapping("/api/system/config-center/secrets/{secretId}/versions")
fun listConfigCenterSecretVersions(
    @PathVariable("secretId") secretId: Long,
): List<ConfigCenterSecretVersionDto> {
    return service().listSecretVersions(secretId)
}

@GetMapping("/api/system/config-center/configs/{configId}/tokens")
fun listConfigCenterServiceTokens(
    @PathVariable("configId") configId: Long,
): List<ConfigCenterServiceTokenDto> {
    return service().listServiceTokens(configId)
}

@PostMapping("/api/system/config-center/tokens")
fun issueConfigCenterServiceToken(
    @RequestBody request: ConfigCenterServiceTokenIssueRequest,
): ConfigCenterServiceTokenIssueResult {
    return service().issueServiceToken(request)
}

@PostMapping("/api/system/config-center/tokens/{tokenId}/revoke")
fun revokeConfigCenterServiceToken(
    @PathVariable("tokenId") tokenId: Long,
): ConfigCenterServiceTokenDto {
    return service().revokeServiceToken(tokenId)
}

@GetMapping("/api/system/config-center/projects/{projectId}/activities")
fun listConfigCenterActivityLogs(
    @PathVariable("projectId") projectId: Long,
    @RequestParam("limit", required = false) limit: Int?,
): List<ConfigCenterActivityLogDto> {
    return service().listActivityLogs(projectId, limit ?: 50)
}

@GetMapping("/api/system/config-center/compat/value")
fun getConfigCenterCompatValue(
    @RequestParam("namespace") namespace: String,
    @RequestParam("key") key: String,
    @RequestParam("profile", required = false) profile: String?,
): ConfigCenterCompatValueDto {
    return service().readCompatValue(
        namespace = namespace,
        key = key,
        profile = profile ?: "default",
    )
}

private fun service(): ConfigCenterService {
    return KoinPlatform.getKoin().get()
}
