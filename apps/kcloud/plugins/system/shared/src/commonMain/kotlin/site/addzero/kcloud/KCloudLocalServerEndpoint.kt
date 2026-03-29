package site.addzero.kcloud

/**
 * 桌面端内嵌服务的本地访问端点。
 */
object KCloudLocalServerEndpoint {
    const val DEFAULT_DESKTOP_PORT: Int = 18080

    private const val DEFAULT_BASE_URL = "http://localhost:$DEFAULT_DESKTOP_PORT/"

    @Volatile
    private var baseUrl: String = DEFAULT_BASE_URL

    fun configureBaseUrl(
        value: String,
    ) {
        baseUrl = value.ifBlank { DEFAULT_BASE_URL }
    }

    fun currentBaseUrl(): String {
        return baseUrl
    }
}
