package site.addzero.vibepocket.music

import site.addzero.vibepocket.api.ServerApiClient
import site.addzero.vibepocket.api.suno.SunoApiClient
import site.addzero.vibepocket.model.ConfigEntry

private const val SUNO_SETUP_COMPLETE_KEY = "vibepocket_setup_complete"

data class SunoRuntimeConfig(
    val apiToken: String = "",
    val baseUrl: String = SunoApiClient.DEFAULT_BASE_URL,
) {
    val hasToken: Boolean
        get() = apiToken.isNotBlank()

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
    val apiToken = ServerApiClient.getConfig("suno_api_token")
        .orEmpty()
        .trim()
    val baseUrl = ServerApiClient.getConfig("suno_api_base_url")
        ?.trim()
        ?.ifBlank { SunoApiClient.DEFAULT_BASE_URL }
        ?: SunoApiClient.DEFAULT_BASE_URL
    return SunoRuntimeConfig(
        apiToken = apiToken,
        baseUrl = baseUrl,
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
) {
    val normalizedToken = apiToken.trim()
    val normalizedBaseUrl = baseUrl.trim()
        .ifBlank { SunoApiClient.DEFAULT_BASE_URL }

    ServerApiClient.configApi.updateConfig(
        ConfigEntry(
            key = "suno_api_token",
            value = normalizedToken,
            description = "Suno API Token",
        )
    )
    ServerApiClient.configApi.updateConfig(
        ConfigEntry(
            key = "suno_api_base_url",
            value = normalizedBaseUrl,
            description = "Suno API Base URL",
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
