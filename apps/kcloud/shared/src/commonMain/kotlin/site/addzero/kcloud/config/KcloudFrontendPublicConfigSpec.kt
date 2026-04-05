package site.addzero.kcloud.config

import site.addzero.configcenter.ConfigCenterItem
import site.addzero.configcenter.ConfigCenterNamespace

@ConfigCenterNamespace(
    namespace = "kcloud",
    objectName = "KcloudFrontendPublicConfigKeys",
)
interface KcloudFrontendPublicConfigSpec {
    @ConfigCenterItem(
        key = "frontend.api.baseUrl",
        comment = "前端公开下发的 API base URL。",
        required = true,
    )
    val frontendApiBaseUrl: String
}
