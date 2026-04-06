package site.addzero.kcloud.server.context

import org.koin.core.annotation.Single
import site.addzero.starter.openapi.spi.OpenApiSpi

@Single
class OpenApiConfig : OpenApiSpi {
    override val enabled: Boolean
        get() = false

    override val openapiPath: String
        get() = "/openapi"

    override val openapiSpec: String
        get() = "openapi/documentation.yaml"
}
