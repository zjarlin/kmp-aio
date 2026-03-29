package site.addzero.kcloud.plugins.system.configcenter

import org.koin.core.annotation.Single
import site.addzero.configcenter.client.ConfigCenter
import site.addzero.configcenter.spec.*

@Single
class ConfigCenterRemoteService {
    suspend fun listEntries(
        query: ConfigQuery,
    ): List<ConfigEntryDto> {
        return ConfigCenter.listEntries(query)
    }

    suspend fun saveEntry(
        request: ConfigMutationRequest,
    ): ConfigEntryDto {
        val requestId = request.id
        return if (requestId.isNullOrBlank()) {
            ConfigCenter.addEnv(request)
        } else {
            ConfigCenter.updateEnv(requestId, request)
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
