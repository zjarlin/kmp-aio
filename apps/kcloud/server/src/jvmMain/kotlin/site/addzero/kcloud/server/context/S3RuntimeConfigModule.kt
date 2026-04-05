package site.addzero.kcloud.server.context

import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.configcenter.ConfigCenterEnv
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
            enabled = env.readBooleanConfig(
                path = S3ConfigKeys.ENABLED,
                defaultValue = false,
            ),
        )
    }

    @Single
    fun provideS3Config(
        env: ConfigCenterEnv,
    ): S3Config {
        return S3Config(
            endpoint = env.requireStringConfig(S3ConfigKeys.ENDPOINT),
            region = env.requireStringConfig(S3ConfigKeys.REGION),
            bucket = env.requireStringConfig(S3ConfigKeys.BUCKET),
            accessKey = env.readStringConfig(S3ConfigKeys.ACCESS_KEY),
            secretKey = env.readStringConfig(S3ConfigKeys.SECRET_KEY),
        )
    }
}

private fun ConfigCenterEnv.readStringConfig(
    path: String,
    defaultValue: String = "",
): String {
    return string(path)
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?: defaultValue
}

private fun ConfigCenterEnv.requireStringConfig(
    path: String,
): String {
    return readStringConfig(path)
        .ifBlank {
            error("缺少配置 $path")
        }
}

private fun ConfigCenterEnv.readBooleanConfig(
    path: String,
    defaultValue: Boolean,
): Boolean {
    val configuredValue = string(path)
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?: defaultValue.toString()
    return configuredValue.toBooleanStrictOrNull()
        ?: error("配置 $path 不是合法的 Boolean：$configuredValue")
}
