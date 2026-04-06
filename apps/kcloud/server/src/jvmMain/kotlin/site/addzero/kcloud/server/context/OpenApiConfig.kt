package site.addzero.kcloud.server.context

import org.koin.core.annotation.Single
import site.addzero.configcenter.ConfigCenterEnv
import site.addzero.starter.openapi.spi.OpenApiSpi

@Single
class OpenApiConfig(
    private val env: ConfigCenterEnv,
) : OpenApiSpi {
    override val enabled: Boolean
        get() = env.boolean("openapi.enabled", false) == true

    override val openapiPath: String
        get() = env.string("openapi.path", "/openapi").orEmpty()

    override val openapiSpec: String
        get() = env.string("openapi.spec", "openapi/documentation.yaml").orEmpty()
}
