package site.addzero.kcloud.plugins.system.configcenter

import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.configcenter.api.Apis
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterValueDto
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterValueWriteRequest

@Single
class ConfigCenterRemoteService {
    suspend fun readValue(
        namespace: String,
        key: String,
        active: String = "dev",
    ): ConfigCenterValueDto {
        return Apis.configCenterApi.getConfigCenterValue(
            namespace = namespace,
            key = key,
            active = active,
        )
    }

    suspend fun writeValue(
        request: ConfigCenterValueWriteRequest,
    ): ConfigCenterValueDto {
        return Apis.configCenterApi.putConfigCenterValue(request)
    }
}
