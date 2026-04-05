package site.addzero.kcloud.music

import site.addzero.kcloud.api.Apis
import site.addzero.kcloud.api.getConfigValueOrNull
import site.addzero.kcloud.api.suno.SunoApiClient
import site.addzero.kcloud.vibepocket.model.ConfigEntry
import site.addzero.kcloud.vibepocket.config.VibepocketConfigKeys

private val defaultSunoApiBaseUrl = VibepocketConfigKeys.sunoApiBaseUrl.defaultValue
    ?: error("缺少 ${VibepocketConfigKeys.SUNO_API_BASE_URL} 的默认值定义。")

data class SunoRuntimeConfig(
    val apiToken: String = "",
    val baseUrl: String = defaultSunoApiBaseUrl,
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
    val apiToken = getConfigValueOrNull(VibepocketConfigKeys.SUNO_API_TOKEN)
        .orEmpty()
        .trim()
    val baseUrl = getConfigValueOrNull(VibepocketConfigKeys.SUNO_API_BASE_URL)
        ?.trim()
        ?.ifBlank { defaultSunoApiBaseUrl }
        ?: defaultSunoApiBaseUrl
    val callbackUrl = getConfigValueOrNull(VibepocketConfigKeys.SUNO_CALLBACK_URL)
        .orEmpty()
        .trim()
    return SunoRuntimeConfig(
        apiToken = apiToken,
        baseUrl = baseUrl,
        callbackUrl = callbackUrl,
    )
}

suspend fun hasCompletedVibePocketSetup(): Boolean {
    val storedValue = getConfigValueOrNull(VibepocketConfigKeys.SUNO_SETUP_COMPLETE)
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
        .ifBlank { defaultSunoApiBaseUrl }
    val normalizedCallbackUrl = callbackUrl.trim()

    Apis.configApi.updateConfig(
        ConfigEntry(
            key = VibepocketConfigKeys.SUNO_API_TOKEN,
            value = normalizedToken,
            description = "Suno API Token",
        )
    )
    Apis.configApi.updateConfig(
        ConfigEntry(
            key = VibepocketConfigKeys.SUNO_API_BASE_URL,
            value = normalizedBaseUrl,
            description = "Suno API Base URL",
        )
    )
    Apis.configApi.updateConfig(
        ConfigEntry(
            key = VibepocketConfigKeys.SUNO_CALLBACK_URL,
            value = normalizedCallbackUrl,
            description = "Suno Callback URL",
        )
    )
    Apis.configApi.updateConfig(
        ConfigEntry(
            key = VibepocketConfigKeys.SUNO_SETUP_COMPLETE,
            value = "true",
            description = "Welcome setup completed",
        )
    )
}
