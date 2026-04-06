package site.addzero.kcloud.server.context

import org.koin.core.annotation.Single
import site.addzero.kcloud.s3.spi.S3ConfigSpi

@Single
class S3Config : S3ConfigSpi {
    override val enabled: Boolean
        get() = false
    override val endpoint: String
        get() = "http://127.0.0.1:9000"
    override val region: String
        get() = "us-east-1"
    override val bucket: String
        get() = "kcloud"
    override val accessKey: String
        get() = ""
    override val secretKey: String
        get() = ""
}
