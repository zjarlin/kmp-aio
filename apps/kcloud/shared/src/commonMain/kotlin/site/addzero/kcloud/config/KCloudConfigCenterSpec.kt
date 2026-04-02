package site.addzero.kcloud.config

import site.addzero.configcenter.ConfigCenterItem
import site.addzero.configcenter.ConfigCenterNamespace

@ConfigCenterNamespace(
    namespace = "kcloud",
    objectName = "KCloudConfigKeys",
)
interface KCloudConfigCenterSpec {
    @ConfigCenterItem(
        key = "ktor.deployment.host",
        comment = "KCloud 服务监听地址。",
        defaultValue = "0.0.0.0",
    )
    val serverHost: String

    @ConfigCenterItem(
        key = "ktor.deployment.port",
        comment = "KCloud 服务监听端口。",
        defaultValue = "8080",
    )
    val serverPort: Int

    @ConfigCenterItem(
        key = "ktor.environment",
        comment = "KCloud 当前环境标识，用于映射 dev/prod 等配置中心 active。",
        defaultValue = "dev",
    )
    val ktorEnvironment: String

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
        defaultValue = "true",
    )
    val sqliteEnabled: Boolean

    @ConfigCenterItem(
        key = "datasources.sqlite.url",
        comment = "SQLite JDBC 地址。",
    )
    val sqliteUrl: String

    @ConfigCenterItem(
        key = "datasources.sqlite.driver",
        comment = "SQLite JDBC Driver。",
        defaultValue = "org.sqlite.JDBC",
    )
    val sqliteDriver: String

    @ConfigCenterItem(
        key = "datasources.postgres.enabled",
        comment = "是否启用 PostgreSQL 数据源。",
        defaultValue = "false",
    )
    val postgresEnabled: Boolean

    @ConfigCenterItem(
        key = "datasources.postgres.url",
        comment = "PostgreSQL JDBC 地址。",
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
        comment = "PostgreSQL JDBC Driver。",
        defaultValue = "org.postgresql.Driver",
    )
    val postgresDriver: String
}
