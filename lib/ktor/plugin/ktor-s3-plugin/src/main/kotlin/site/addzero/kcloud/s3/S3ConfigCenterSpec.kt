package site.addzero.kcloud.s3

import site.addzero.configcenter.ConfigCenterItem
import site.addzero.configcenter.ConfigCenterNamespace

@ConfigCenterNamespace(
    namespace = "kcloud",
    objectName = "S3ConfigKeys",
)
interface S3ConfigCenterSpec {
    @ConfigCenterItem(
        key = "s3.enabled",
        comment = "是否启用 S3 自动装配。",
        defaultValue = "true",
    )
    val enabled: Boolean

    @ConfigCenterItem(
        key = "s3.endpoint",
        comment = "S3 endpoint。",
        defaultValue = "https://s3.cstcloud.cn",
    )
    val endpoint: String

    @ConfigCenterItem(
        key = "s3.region",
        comment = "S3 region。",
        defaultValue = "us-east-1",
    )
    val region: String

    @ConfigCenterItem(
        key = "s3.bucket",
        comment = "S3 bucket 名称。",
        defaultValue = "af466fd92b0146ccbfb40cf590c912a0",
    )
    val bucket: String

    @ConfigCenterItem(
        key = "s3.accessKey",
        comment = "S3 accessKey。",
    )
    val accessKey: String

    @ConfigCenterItem(
        key = "s3.secretKey",
        comment = "S3 secretKey。",
    )
    val secretKey: String
}
