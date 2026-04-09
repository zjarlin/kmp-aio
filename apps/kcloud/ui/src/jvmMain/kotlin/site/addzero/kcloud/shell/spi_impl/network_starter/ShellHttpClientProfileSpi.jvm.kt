package site.addzero.kcloud.shell.spi_impl.network_starter

private const val KCLOUD_BASE_URL_PROPERTY = "site.addzero.kcloud.baseUrl"

internal actual fun resolveNetworkStarterBaseUrl(): String {
    return System.getProperty(KCLOUD_BASE_URL_PROPERTY)
        ?.takeIf { it.isNotBlank() }
        ?: "http://$DEFAULT_SERVER_HOST:$DEFAULT_SERVER_PORT/"
}
