package site.addzero.starter.openapi

import site.addzero.configcenter.ConfigCenterItem
import site.addzero.configcenter.ConfigCenterNamespace

@ConfigCenterNamespace(
    namespace = "kcloud",
    objectName = "OpenApiConfigKeys",
)
interface OpenApiConfigCenterSpec {
    @ConfigCenterItem(
        key = "openapi.enabled",
        comment = "是否启用 OpenAPI / Swagger UI。",
        defaultValue = "true",
    )
    val enabled: Boolean

    @ConfigCenterItem(
        key = "openapi.path",
        comment = "Swagger UI 访问路径。",
        defaultValue = "/swagger",
    )
    val path: String

    @ConfigCenterItem(
        key = "openapi.spec",
        comment = "OpenAPI 文档资源路径。",
        defaultValue = "openapi/documentation.yaml",
    )
    val spec: String
}
