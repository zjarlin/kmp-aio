package site.addzero.kcloud.server.context

import org.koin.core.annotation.Single
import site.addzero.starter.openapi.spi.OpenApiSpi

@Single
class OpenApiConfig(
    private val config: ServerContextConfig,
) : OpenApiSpi {
    override val enabled: Boolean
        get() = config.openApi.enabled

    override val openapiPath: String
        get() = config.openApi.path

    override val openapiSpec: String
        get() = config.openApi.spec
}
