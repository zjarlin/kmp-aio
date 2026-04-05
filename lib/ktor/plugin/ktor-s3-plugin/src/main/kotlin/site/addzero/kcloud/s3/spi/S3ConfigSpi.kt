package site.addzero.kcloud.s3.spi

interface S3ConfigSpi {

    val enabled: Boolean
    val endpoint: String
    val region: String
    val bucket: String
    val accessKey: String
    val secretKey: String
}