package site.addzero.kcloud.plugins.system.configcenter

import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.configcenter.api.*

@Single
class ConfigCenterRemoteService {
    suspend fun listProjects(): List<ConfigCenterProjectDto> {
        return ConfigCenterApiClient.configCenterApi.listConfigCenterProjects()
    }

    suspend fun createProject(
        request: ConfigCenterProjectMutationRequest,
    ): ConfigCenterProjectDto {
        return ConfigCenterApiClient.configCenterApi.createConfigCenterProject(request)
    }

    suspend fun updateProject(
        projectId: Long,
        request: ConfigCenterProjectMutationRequest,
    ): ConfigCenterProjectDto {
        return ConfigCenterApiClient.configCenterApi.updateConfigCenterProject(projectId, request)
    }

    suspend fun listEnvironments(
        projectId: Long,
    ): List<ConfigCenterEnvironmentDto> {
        return ConfigCenterApiClient.configCenterApi.listConfigCenterEnvironments(projectId)
    }

    suspend fun createEnvironment(
        projectId: Long,
        request: ConfigCenterEnvironmentMutationRequest,
    ): ConfigCenterEnvironmentDto {
        return ConfigCenterApiClient.configCenterApi.createConfigCenterEnvironment(projectId, request)
    }

    suspend fun updateEnvironment(
        projectId: Long,
        environmentId: Long,
        request: ConfigCenterEnvironmentMutationRequest,
    ): ConfigCenterEnvironmentDto {
        return ConfigCenterApiClient.configCenterApi.updateConfigCenterEnvironment(projectId, environmentId, request)
    }

    suspend fun listConfigs(
        projectId: Long,
    ): List<ConfigCenterConfigDto> {
        return ConfigCenterApiClient.configCenterApi.listConfigCenterConfigs(projectId)
    }

    suspend fun createConfig(
        projectId: Long,
        request: ConfigCenterConfigMutationRequest,
    ): ConfigCenterConfigDto {
        return ConfigCenterApiClient.configCenterApi.createConfigCenterConfig(projectId, request)
    }

    suspend fun updateConfig(
        configId: Long,
        request: ConfigCenterConfigMutationRequest,
    ): ConfigCenterConfigDto {
        return ConfigCenterApiClient.configCenterApi.updateConfigCenterConfig(configId, request)
    }

    suspend fun listSecrets(
        configId: Long,
        includeInherited: Boolean,
    ): List<ConfigCenterSecretDto> {
        return ConfigCenterApiClient.configCenterApi.listConfigCenterSecrets(configId, includeInherited)
    }

    suspend fun createSecret(
        request: ConfigCenterSecretMutationRequest,
    ): ConfigCenterSecretDto {
        return ConfigCenterApiClient.configCenterApi.createConfigCenterSecret(request)
    }

    suspend fun updateSecret(
        secretId: Long,
        request: ConfigCenterSecretMutationRequest,
    ): ConfigCenterSecretDto {
        return ConfigCenterApiClient.configCenterApi.updateConfigCenterSecret(secretId, request)
    }

    suspend fun deleteSecret(
        secretId: Long,
    ) {
        ConfigCenterApiClient.configCenterApi.deleteConfigCenterSecret(secretId)
    }

    suspend fun listSecretVersions(
        secretId: Long,
    ): List<ConfigCenterSecretVersionDto> {
        return ConfigCenterApiClient.configCenterApi.listConfigCenterSecretVersions(secretId)
    }

    suspend fun listTokens(
        configId: Long,
    ): List<ConfigCenterServiceTokenDto> {
        return ConfigCenterApiClient.configCenterApi.listConfigCenterServiceTokens(configId)
    }

    suspend fun issueToken(
        request: ConfigCenterServiceTokenIssueRequest,
    ): ConfigCenterServiceTokenIssueResult {
        return ConfigCenterApiClient.configCenterApi.issueConfigCenterServiceToken(request)
    }

    suspend fun revokeToken(
        tokenId: Long,
    ): ConfigCenterServiceTokenDto {
        return ConfigCenterApiClient.configCenterApi.revokeConfigCenterServiceToken(tokenId)
    }

    suspend fun listActivities(
        projectId: Long,
        limit: Int = 80,
    ): List<ConfigCenterActivityLogDto> {
        return ConfigCenterApiClient.configCenterApi.listConfigCenterActivityLogs(projectId, limit)
    }
}
