package site.addzero.kcloud.server.context

import org.koin.core.annotation.Single
import site.addzero.kcloud.s3.spi.S3ConfigSpi

@Single
class S3Config(
    private val config: ServerContextConfig,
) : S3ConfigSpi {
    override val enabled: Boolean
        get() = config.s3.enabled
    override val endpoint: String
        get() = config.s3.endpoint
    override val region: String
        get() = config.s3.region
    override val bucket: String
        get() = config.s3.bucket
    override val accessKey: String
        get() = config.s3.accessKey
    override val secretKey: String
        get() = config.s3.secretKey
}
