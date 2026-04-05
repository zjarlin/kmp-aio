package site.addzero.kcloud.api

internal suspend fun getConfigValueOrNull(
    key: String,
): String? {
    return runCatching {
        ApiProvider.configApi.getConfig(key).value
    }.getOrNull()
}
