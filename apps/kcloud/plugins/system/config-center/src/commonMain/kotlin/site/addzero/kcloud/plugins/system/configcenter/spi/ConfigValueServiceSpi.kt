package site.addzero.kcloud.plugins.system.configcenter.spi

import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterValueDto

/**
 * 配置中心对外暴露的最小值读写契约。
 *
 * 其它插件只能依赖这个共享 SPI，不直接依赖 `config-center:server` 里的实现类。
 */
interface ConfigValueServiceSpi {
    fun readValue(
        namespace: String,
        key: String,
        active: String,
    ): ConfigCenterValueDto

    fun writeValue(
        namespace: String,
        key: String,
        value: String,
        active: String,
    ): ConfigCenterValueDto
}

const val RUNTIME_CONFIG_CENTER_ACTIVE_KEY = "ktor.environment"

fun requireRuntimeConfigCenterActive(
    rawValue: String?,
): String {
    val normalized = rawValue
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?: error("缺少启动配置 $RUNTIME_CONFIG_CENTER_ACTIVE_KEY，无法确定配置中心 active。")

    return when (normalized.lowercase()) {
        "default",
        "dev",
        "development",
        -> "dev"

        "prod",
        "prd",
        "production",
        -> "prod"

        else -> {
            normalized
                .lowercase()
                .replace(Regex("[^a-z0-9]+"), "-")
                .trim('-')
                .ifBlank {
                    error("启动配置 $RUNTIME_CONFIG_CENTER_ACTIVE_KEY=$normalized 无法归一化为有效 active。")
                }
        }
    }
}
