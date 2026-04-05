package site.addzero.kcloud.server.context

import org.koin.core.annotation.Single
import site.addzero.configcenter.ConfigCenterEnv
import site.addzero.kcloud.s3.S3ConfigKeys
import site.addzero.kcloud.s3.spi.S3ConfigSpi

@Single
class S3Config(
    private val env: ConfigCenterEnv,
) : S3ConfigSpi {
    override val enabled: Boolean
        get() = env.boolean(S3ConfigKeys.ENABLED, false) == true
    override val endpoint: String
        get() = requiredValue(S3ConfigKeys.ENDPOINT)
    override val region: String
        get() = requiredValue(S3ConfigKeys.REGION)
    override val bucket: String
        get() = requiredValue(S3ConfigKeys.BUCKET)
    override val accessKey: String
        get() = env.string(S3ConfigKeys.ACCESS_KEY).orEmpty()
    override val secretKey: String
        get() = env.string(S3ConfigKeys.SECRET_KEY).orEmpty()

    private fun requiredValue(path: String): String {
        return env.string(path)
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?: error("缺少配置 $path")
    }
}

