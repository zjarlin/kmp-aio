package site.addzero.starter.openapi.spi

interface OpenApiSpi {
    val enabled
        get() = true
    val openapiPath
        get() = "/swagger"
    val openapiSpec
        get() = "openapi/documentation.yaml"
}