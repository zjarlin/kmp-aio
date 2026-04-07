package site.addzero.kcloud.shell.spi_impl.network_starter

import site.addzero.kcloud.runtime.KCloudHostRuntime

private const val KCLOUD_BASE_URL_PROPERTY = "site.addzero.kcloud.baseUrl"

internal actual fun resolveNetworkStarterBaseUrl(): String {
    return System.getProperty(KCLOUD_BASE_URL_PROPERTY)
        ?.takeIf { it.isNotBlank() }
        ?: KCloudHostRuntime.resolveBaseUrl()
}
