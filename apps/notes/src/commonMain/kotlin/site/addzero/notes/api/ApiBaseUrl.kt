package site.addzero.notes.api

import site.addzero.notes.BuildKonfig

internal fun configuredApiBaseUrl(fallback: String): String {
    val configured = BuildKonfig.BASE_URL.trim()
    if (configured.isNotBlank()) {
        return configured
    }
    return fallback
}
