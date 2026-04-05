package site.addzero.kcloud.vibepocket.config

import site.addzero.configcenter.ConfigCenterItem
import site.addzero.configcenter.ConfigCenterNamespace

@ConfigCenterNamespace(
    namespace = "vibepocket",
    objectName = "VibepocketConfigKeys",
)
interface VibepocketConfigCenterSpec {
    @ConfigCenterItem(
        key = "storage.type",
        comment = "VibePocket 当前存储类型，例如 LOCAL、S3。",
        required = true,
    )
    val storageType: String

    @ConfigCenterItem(
        key = "storage.endpoint",
        comment = "对象存储或远端存储服务 endpoint。",
    )
    val storageEndpoint: String

    @ConfigCenterItem(
        key = "storage.accessKey",
        comment = "对象存储 accessKey。",
    )
    val storageAccessKey: String

    @ConfigCenterItem(
        key = "storage.secretKey",
        comment = "对象存储 secretKey。",
    )
    val storageSecretKey: String

    @ConfigCenterItem(
        key = "storage.bucketName",
        comment = "对象存储 bucket 名称。",
    )
    val storageBucketName: String

    @ConfigCenterItem(
        key = "storage.region",
        comment = "对象存储 region。",
    )
    val storageRegion: String

    @ConfigCenterItem(
        key = "storage.domain",
        comment = "对象存储对外访问域名。",
    )
    val storageDomain: String

    @ConfigCenterItem(
        key = "storage.basePath",
        comment = "对象存储基础路径前缀。",
    )
    val storageBasePath: String
}
