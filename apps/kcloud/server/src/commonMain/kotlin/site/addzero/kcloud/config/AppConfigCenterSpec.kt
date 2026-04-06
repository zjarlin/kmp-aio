package site.addzero.kcloud.config

import site.addzero.configcenter.ConfigCenterItem
import site.addzero.configcenter.ConfigCenterNamespace

@ConfigCenterNamespace(
    namespace = "kcloud",
    objectName = "AppConfigKeys",
)
interface AppConfigCenterSpec {
    @ConfigCenterItem(
        key = "ktor.environment",
        comment = "Ktor 当前激活环境。",
    )
    val ktorEnvironment: String

    @ConfigCenterItem(
        key = "server.host",
        comment = "主服务监听地址。",
    )
    val serverHost: String

    @ConfigCenterItem(
        key = "server.port",
        comment = "主服务监听端口。",
    )
    val serverPort: Int

    @ConfigCenterItem(
        key = "desktop.server.public-host",
        comment = "桌面内嵌模式下前端访问服务端使用的主机名。",
    )
    val desktopServerPublicHost: String

    @ConfigCenterItem(
        key = "desktop.server.port",
        comment = "桌面内嵌模式下服务端监听端口。",
    )
    val desktopServerPort: Int

    @ConfigCenterItem(
        key = "desktop.app-directory-name",
        comment = "桌面模式应用数据目录名称。",
    )
    val desktopAppDirectoryName: String

    @ConfigCenterItem(
        key = "desktop.sqlite-file-name",
        comment = "桌面模式 SQLite 文件名。",
    )
    val desktopSqliteFileName: String

    @ConfigCenterItem(
        key = "desktop.banner.text",
        comment = "桌面模式 Banner 主标题。",
    )
    val desktopBannerText: String

    @ConfigCenterItem(
        key = "desktop.banner.subtitle",
        comment = "桌面模式 Banner 副标题。",
    )
    val desktopBannerSubtitle: String

    @ConfigCenterItem(
        key = "desktop.openapi.enabled",
        comment = "桌面模式是否启用 OpenAPI 页面。",
    )
    val desktopOpenapiEnabled: Boolean

    @ConfigCenterItem(
        key = "desktop.openapi.path",
        comment = "桌面模式 OpenAPI UI 路径。",
    )
    val desktopOpenapiPath: String

    @ConfigCenterItem(
        key = "desktop.openapi.spec",
        comment = "桌面模式 OpenAPI 文档资源路径。",
    )
    val desktopOpenapiSpec: String

    @ConfigCenterItem(
        key = "desktop.flyway.enabled",
        comment = "桌面模式是否执行 Flyway。",
    )
    val desktopFlywayEnabled: Boolean

    @ConfigCenterItem(
        key = "desktop.s3.enabled",
        comment = "桌面模式是否启用 S3。",
    )
    val desktopS3Enabled: Boolean

    @ConfigCenterItem(
        key = "datasources.sqlite.enabled",
        comment = "是否启用 SQLite 数据源。",
    )
    val sqliteEnabled: Boolean

    @ConfigCenterItem(
        key = "datasources.sqlite.url",
        comment = "SQLite 数据源 JDBC 地址。",
    )
    val sqliteUrl: String

    @ConfigCenterItem(
        key = "datasources.sqlite.driver",
        comment = "SQLite 数据源驱动类名。",
    )
    val sqliteDriver: String

    @ConfigCenterItem(
        key = "datasources.postgres.enabled",
        comment = "是否启用 Postgres 数据源。",
    )
    val postgresEnabled: Boolean
}
