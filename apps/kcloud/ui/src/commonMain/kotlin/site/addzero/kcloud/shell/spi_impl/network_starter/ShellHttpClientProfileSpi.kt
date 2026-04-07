package site.addzero.kcloud.shell.spi_impl.network_starter

import org.koin.core.annotation.Single
import site.addzero.core.network.spi.HttpClientProfileSpi

/**
 * 壳层默认网络配置。
 *
 * `network-starter` 当前只接受单个 `HttpClientProfileSpi`，
 * 所以这里由应用壳层统一提供默认 baseUrl。
 */
@Single
class ShellHttpClientProfileSpi : HttpClientProfileSpi {
    override val enableCurlLogging: Boolean = false

    override val baseUrl: String = resolveNetworkStarterBaseUrl()
}

internal expect fun resolveNetworkStarterBaseUrl(): String
