package site.addzero.kcloud.config

import kotlinx.serialization.Serializable

const val KCLOUD_FRONTEND_RUNTIME_CONFIG_FILE_NAME = "kcloud-runtime-config.json"

@Serializable
data class KcloudFrontendRuntimeConfig(
    val apiBaseUrl: String,
) {
    init {
        requireValidApiBaseUrl(apiBaseUrl)
    }

    fun normalizedApiBaseUrl(): String {
        return requireValidApiBaseUrl(apiBaseUrl)
    }
}

private val HTTP_BASE_URL_PATTERN = Regex("^https?://[^/\\s]+(?:/.*)?/$")

fun requireValidApiBaseUrl(
    value: String,
): String {
    val normalized = value.trim()
        .trimEnd('/')
        .plus("/")
    require(normalized.isNotBlank()) {
        "KcloudFrontendRuntimeConfig.apiBaseUrl 不能为空。"
    }
    require(HTTP_BASE_URL_PATTERN.matches(normalized)) {
        "KcloudFrontendRuntimeConfig.apiBaseUrl 必须是合法的 http/https URL：$normalized"
    }
    return normalized
}
