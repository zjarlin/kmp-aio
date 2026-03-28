package site.addzero.kcloud.plugins.system.configcenter

import site.addzero.configcenter.client.ConfigCenter
import site.addzero.configcenter.spec.ConfigEntryDto
import site.addzero.configcenter.spec.ConfigMutationRequest
import site.addzero.configcenter.spec.ConfigQuery
import site.addzero.configcenter.spec.ConfigTargetDto
import site.addzero.configcenter.spec.ConfigTargetMutationRequest
import site.addzero.configcenter.spec.ConfigValueResponse
import site.addzero.configcenter.spec.RenderedConfig

class ConfigCenterRemoteService {
    suspend fun listEntries(
        query: ConfigQuery,
    ): List<ConfigEntryDto> {
        return ConfigCenter.listEntries(query)
    }

    suspend fun saveEntry(
        request: ConfigMutationRequest,
    ): ConfigEntryDto {
        return if (request.id.isNullOrBlank()) {
            ConfigCenter.addEnv(request)
        } else {
            ConfigCenter.updateEnv(request.id, request)
        }
    }

    suspend fun deleteEntry(
        id: String,
    ) {
        ConfigCenter.deleteEnv(id)
    }

    suspend fun listTargets(): List<ConfigTargetDto> {
        return ConfigCenter.listTargets()
    }

    suspend fun saveTarget(
        request: ConfigTargetMutationRequest,
    ): ConfigTargetDto {
        return ConfigCenter.saveTarget(request)
    }

    suspend fun deleteTarget(
        id: String,
    ) {
        ConfigCenter.deleteTarget(id)
    }

    suspend fun previewTarget(
        targetId: String,
    ): String {
        return ConfigCenter.previewTarget(targetId)
    }

    suspend fun exportTarget(
        targetId: String,
    ): RenderedConfig {
        return ConfigCenter.exportTarget(targetId)
    }

    suspend fun readBootstrapValue(
        key: String,
    ): ConfigValueResponse {
        return site.addzero.configcenter.client.ConfigCenterApiClient
            .createApi()
            .getBootstrapValue(key)
    }
}

