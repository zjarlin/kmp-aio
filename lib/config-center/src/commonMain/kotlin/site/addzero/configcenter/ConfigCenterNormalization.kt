package site.addzero.configcenter

fun normalizeConfigCenterNamespace(
    rawValue: String,
): String {
    return rawValue
        .trim()
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
}

fun normalizeConfigCenterActive(
    rawValue: String,
): String {
    val normalized = rawValue.trim().lowercase().ifBlank { DEFAULT_CONFIG_CENTER_ACTIVE }
    return when (normalized) {
        "default",
        "dev",
        "development",
        -> "dev"

        "prod",
        "prd",
        "production",
        -> "prod"

        else -> normalizeConfigCenterNamespace(normalized).ifBlank { DEFAULT_CONFIG_CENTER_ACTIVE }
    }
}
