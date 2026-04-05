package site.addzero.kcloud.server

import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.configcenter.ConfigCenterEnv
import site.addzero.configcenter.ConfigCenterKeyDefinition
import site.addzero.kcloud.s3.S3Config
import site.addzero.kcloud.s3.S3ConfigKeys
import site.addzero.kcloud.s3.S3RuntimeToggle

@Module
class S3RuntimeConfigModule {
    @Single
    fun provideS3RuntimeToggle(
        env: ConfigCenterEnv,
    ): S3RuntimeToggle {
        return S3RuntimeToggle(
            enabled = env.readBooleanConfig(S3ConfigKeys.enabled),
        )
    }

    @Single
    fun provideS3Config(
        env: ConfigCenterEnv,
    ): S3Config {
        return S3Config(
            endpoint = env.requireStringConfig(S3ConfigKeys.endpoint),
            region = env.requireStringConfig(S3ConfigKeys.region),
            bucket = env.requireStringConfig(S3ConfigKeys.bucket),
            accessKey = env.readStringConfig(S3ConfigKeys.accessKey),
            secretKey = env.readStringConfig(S3ConfigKeys.secretKey),
        )
    }
}

private fun ConfigCenterEnv.readStringConfig(
    definition: ConfigCenterKeyDefinition,
): String {
    return string(definition)
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?: definition.defaultValue.orEmpty()
}

private fun ConfigCenterEnv.requireStringConfig(
    definition: ConfigCenterKeyDefinition,
): String {
    return readStringConfig(definition)
        .ifBlank {
            error("缺少配置 ${definition.namespace}.${definition.key}")
        }
}

private fun ConfigCenterEnv.readBooleanConfig(
    definition: ConfigCenterKeyDefinition,
): Boolean {
    val configuredValue = string(definition)
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?: definition.defaultValue.orEmpty()
    return configuredValue.toBooleanStrictOrNull()
        ?: error("配置 ${definition.namespace}.${definition.key} 不是合法的 Boolean：$configuredValue")
}
