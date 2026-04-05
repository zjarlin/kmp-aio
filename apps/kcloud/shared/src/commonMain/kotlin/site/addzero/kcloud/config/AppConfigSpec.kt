package site.addzero.kcloud.config

import site.addzero.configcenter.ConfigCenterItem
import site.addzero.configcenter.ConfigCenterNamespace

@ConfigCenterNamespace(
    namespace = "kcloud",
    objectName = "AppConfigKeys",
)
interface AppConfigSpec {
    @ConfigCenterItem(
        key = "ktor.deployment.host",
        comment = "KCloud 服务监听地址。",
        required = true,
    )
    val serverHost: String

    @ConfigCenterItem(
        key = "ktor.deployment.port",
        comment = "KCloud 服务监听端口。",
        required = true,
    )
    val serverPort: Int

    @ConfigCenterItem(
        key = "ktor.environment",
        comment = "KCloud 当前环境标识，用于映射 dev/prod 等配置中心 active。",
        required = true,
    )
    val ktorEnvironment: String

    @ConfigCenterItem(
        key = "desktop.server.publicHost",
        comment = "桌面内嵌服务回写给 UI 使用的主机名。",
        required = true,
    )
    val desktopServerPublicHost: String

    @ConfigCenterItem(
        key = "desktop.server.defaultPort",
        comment = "桌面内嵌服务默认端口。",
        required = true,
    )
    val desktopServerPort: Int

    @ConfigCenterItem(
        key = "desktop.app.directoryName",
        comment = "桌面端数据与缓存目录的应用目录名。",
        required = true,
    )
    val desktopAppDirectoryName: String

    @ConfigCenterItem(
        key = "desktop.sqlite.fileName",
        comment = "桌面端 SQLite 文件名。",
        required = true,
    )
    val desktopSqliteFileName: String

    @ConfigCenterItem(
        key = "desktop.banner.text",
        comment = "桌面内嵌服务 Banner 标题。",
        required = true,
    )
    val desktopBannerText: String

    @ConfigCenterItem(
        key = "desktop.banner.subtitle",
        comment = "桌面内嵌服务 Banner 副标题。",
        required = true,
    )
    val desktopBannerSubtitle: String

    @ConfigCenterItem(
        key = "desktop.openapi.enabled",
        comment = "桌面内嵌服务是否启用 OpenAPI。",
        required = true,
    )
    val desktopOpenapiEnabled: Boolean

    @ConfigCenterItem(
        key = "desktop.openapi.path",
        comment = "桌面内嵌服务 OpenAPI 页面路径。",
        required = true,
    )
    val desktopOpenapiPath: String

    @ConfigCenterItem(
        key = "desktop.openapi.spec",
        comment = "桌面内嵌服务 OpenAPI 文档路径。",
        required = true,
    )
    val desktopOpenapiSpec: String

    @ConfigCenterItem(
        key = "desktop.flyway.enabled",
        comment = "桌面内嵌服务是否启用 Flyway。",
        required = true,
    )
    val desktopFlywayEnabled: Boolean

    @ConfigCenterItem(
        key = "desktop.s3.enabled",
        comment = "桌面内嵌服务是否启用 S3。",
        required = true,
    )
    val desktopS3Enabled: Boolean

    @ConfigCenterItem(
        key = "kcloud.dataDir",
        comment = "KCloud 业务数据目录。",
    )
    val dataDir: String

    @ConfigCenterItem(
        key = "kcloud.cacheDir",
        comment = "KCloud 缓存目录。",
    )
    val cacheDir: String

    @ConfigCenterItem(
        key = "datasources.sqlite.enabled",
        comment = "是否启用 SQLite 数据源。",
        required = true,
    )
    val sqliteEnabled: Boolean

    @ConfigCenterItem(
        key = "datasources.sqlite.url",
        comment = "SQLite 数据源的 JDBC 连接地址。",
    )
    val sqliteUrl: String

    @ConfigCenterItem(
        key = "datasources.sqlite.driver",
        comment = "SQLite 数据源的 JDBC 驱动类名。",
        required = true,
    )
    val sqliteDriver: String

    @ConfigCenterItem(
        key = "datasources.postgres.enabled",
        comment = "是否启用 PostgreSQL 数据源。",
        required = true,
    )
    val postgresEnabled: Boolean

    @ConfigCenterItem(
        key = "datasources.postgres.url",
        comment = "PostgreSQL 数据源的 JDBC 连接地址。",
    )
    val postgresUrl: String

    @ConfigCenterItem(
        key = "datasources.postgres.user",
        comment = "PostgreSQL 用户名。",
    )
    val postgresUser: String

    @ConfigCenterItem(
        key = "datasources.postgres.password",
        comment = "PostgreSQL 密码。",
    )
    val postgresPassword: String

    @ConfigCenterItem(
        key = "datasources.postgres.driver",
        comment = "PostgreSQL 数据源的 JDBC 驱动类名。",
        required = true,
    )
    val postgresDriver: String
}
