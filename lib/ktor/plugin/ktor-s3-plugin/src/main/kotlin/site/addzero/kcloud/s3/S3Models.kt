package site.addzero.kcloud.s3

data class S3Config(
    val endpoint: String,
    val region: String,
    val bucket: String,
    val accessKey: String,
    val secretKey: String,
)

interface S3ConfigSpi{

}

data class S3RuntimeToggle(
    val enabled: Boolean,
)
