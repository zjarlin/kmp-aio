package site.addzero.kcloud.shell.spi_impl.network_starter

import site.addzero.kcloud.runtime.KCloudHostRuntime

internal actual fun resolveNetworkStarterBaseUrl(): String {
    return KCloudHostRuntime.resolveBaseUrl()
}
