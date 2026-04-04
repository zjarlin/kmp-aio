package site.addzero.kcloud.music

import site.addzero.kcloud.api.ServerApiClient
import site.addzero.kcloud.api.suno.SunoApiClient
import site.addzero.kcloud.vibepocket.model.ConfigEntry

private const val SUNO_SETUP_COMPLETE_KEY = "vibepocket_setup_complete"
private const val SUNO_API_TOKEN_KEY = "suno_api_token"
private const val SUNO_API_BASE_URL_KEY = "suno_api_base_url"
private const val SUNO_CALLBACK_URL_KEY = "suno_callback_url"

data class SunoRuntimeConfig(
    val apiToken: String = "",
    val baseUrl: String = SunoApiClient.DEFAULT_BASE_URL,
    val callbackUrl: String = "",
) {
    val hasToken
        get() = apiToken.isNotBlank()

    val hasCallbackUrl
        get() = callbackUrl.isNotBlank()

    fun callbackUrlOrNull(): String? {
        return callbackUrl.trim().ifBlank { null }
    }

    fun requireToken() {
        if (!hasToken) {
            throw IllegalStateException(SunoApiClient.MISSING_API_TOKEN_MESSAGE)
        }
    }

    fun createClient(): SunoApiClient {
        return SunoApiClient(
            apiToken = apiToken,
            baseUrl = baseUrl,
        )
    }
}

suspend fun loadSunoRuntimeConfig(): SunoRuntimeConfig {
    val apiToken = ServerApiClient.getConfig(SUNO_API_TOKEN_KEY)
        .orEmpty()
        .trim()
    val baseUrl = ServerApiClient.getConfig(SUNO_API_BASE_URL_KEY)
        ?.trim()
        ?.ifBlank { SunoApiClient.DEFAULT_BASE_URL }
        ?: SunoApiClient.DEFAULT_BASE_URL
    val callbackUrl = ServerApiClient.getConfig(SUNO_CALLBACK_URL_KEY)
        .orEmpty()
        .trim()
    return SunoRuntimeConfig(
        apiToken = apiToken,
        baseUrl = baseUrl,
        callbackUrl = callbackUrl,
    )
}

suspend fun hasCompletedVibePocketSetup(): Boolean {
    val storedValue = ServerApiClient.getConfig(SUNO_SETUP_COMPLETE_KEY)
        ?.trim()
        ?.equals("true", ignoreCase = true)
    return storedValue == true
}

suspend fun persistSunoRuntimeConfig(
    apiToken: String,
    baseUrl: String,
    callbackUrl: String = "",
) {
    val normalizedToken = apiToken.trim()
    val normalizedBaseUrl = baseUrl.trim()
        .ifBlank { SunoApiClient.DEFAULT_BASE_URL }
    val normalizedCallbackUrl = callbackUrl.trim()

    ServerApiClient.configApi.updateConfig(
        ConfigEntry(
            key = SUNO_API_TOKEN_KEY,
            value = normalizedToken,
            description = "Suno API Token",
        )
    )
    ServerApiClient.configApi.updateConfig(
        ConfigEntry(
            key = SUNO_API_BASE_URL_KEY,
            value = normalizedBaseUrl,
            description = "Suno API Base URL",
        )
    )
    ServerApiClient.configApi.updateConfig(
        ConfigEntry(
            key = SUNO_CALLBACK_URL_KEY,
            value = normalizedCallbackUrl,
            description = "Suno Callback URL",
        )
    )
    ServerApiClient.configApi.updateConfig(
        ConfigEntry(
            key = SUNO_SETUP_COMPLETE_KEY,
            value = "true",
            description = "Welcome setup completed",
        )
    )
}
