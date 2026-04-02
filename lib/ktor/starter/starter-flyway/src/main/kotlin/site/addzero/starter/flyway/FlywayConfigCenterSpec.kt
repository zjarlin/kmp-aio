package site.addzero.starter.flyway

import site.addzero.configcenter.ConfigCenterItem
import site.addzero.configcenter.ConfigCenterNamespace

@ConfigCenterNamespace(
    namespace = "kcloud",
    objectName = "FlywayConfigKeys",
)
interface FlywayConfigCenterSpec {
    @ConfigCenterItem(
        key = "flyway.enabled",
        comment = "是否启用 Flyway 自动迁移。",
        defaultValue = "true",
    )
    val enabled: Boolean

    @ConfigCenterItem(
        key = "flyway.locations",
        comment = "Flyway 全局迁移脚本位置列表。",
    )
    val locations: String

    @ConfigCenterItem(
        key = "flyway.baseline-on-migrate",
        comment = "Flyway baselineOnMigrate 开关。",
        defaultValue = "false",
    )
    val baselineOnMigrate: Boolean

    @ConfigCenterItem(
        key = "flyway.baseline-version",
        comment = "Flyway baselineVersion。",
        defaultValue = "0",
    )
    val baselineVersion: String

    @ConfigCenterItem(
        key = "flyway.clean-disabled",
        comment = "Flyway cleanDisabled 开关。",
        defaultValue = "true",
    )
    val cleanDisabled: Boolean

    @ConfigCenterItem(
        key = "flyway.connect-retries",
        comment = "Flyway 连接重试次数。",
        defaultValue = "0",
    )
    val connectRetries: Int

    @ConfigCenterItem(
        key = "flyway.group",
        comment = "Flyway group 开关。",
        defaultValue = "false",
    )
    val group: Boolean

    @ConfigCenterItem(
        key = "flyway.installed-by",
        comment = "Flyway installedBy。",
    )
    val installedBy: String

    @ConfigCenterItem(
        key = "flyway.mixed",
        comment = "Flyway mixed 开关。",
        defaultValue = "false",
    )
    val mixed: Boolean

    @ConfigCenterItem(
        key = "flyway.out-of-order",
        comment = "Flyway outOfOrder 开关。",
        defaultValue = "false",
    )
    val outOfOrder: Boolean

    @ConfigCenterItem(
        key = "flyway.placeholder-replacement",
        comment = "Flyway placeholderReplacement 开关。",
        defaultValue = "true",
    )
    val placeholderReplacement: Boolean

    @ConfigCenterItem(
        key = "flyway.placeholder-prefix",
        comment = "Flyway placeholder 前缀。",
        defaultValue = "\${",
    )
    val placeholderPrefix: String

    @ConfigCenterItem(
        key = "flyway.placeholder-suffix",
        comment = "Flyway placeholder 后缀。",
        defaultValue = "}",
    )
    val placeholderSuffix: String

    @ConfigCenterItem(
        key = "flyway.placeholders.{name}",
        comment = "Flyway 全局占位符 {name}。",
    )
    val placeholderEntry: String

    @ConfigCenterItem(
        key = "flyway.schemas",
        comment = "Flyway schemas 列表。",
    )
    val schemas: String

    @ConfigCenterItem(
        key = "flyway.skip-default-callbacks",
        comment = "Flyway skipDefaultCallbacks 开关。",
        defaultValue = "false",
    )
    val skipDefaultCallbacks: Boolean

    @ConfigCenterItem(
        key = "flyway.skip-default-resolvers",
        comment = "Flyway skipDefaultResolvers 开关。",
        defaultValue = "false",
    )
    val skipDefaultResolvers: Boolean

    @ConfigCenterItem(
        key = "flyway.sql-migration-prefix",
        comment = "Flyway SQL migration prefix。",
        defaultValue = "V",
    )
    val sqlMigrationPrefix: String

    @ConfigCenterItem(
        key = "flyway.sql-migration-separator",
        comment = "Flyway SQL migration separator。",
        defaultValue = "__",
    )
    val sqlMigrationSeparator: String

    @ConfigCenterItem(
        key = "flyway.sql-migration-suffixes",
        comment = "Flyway SQL migration suffix 列表。",
    )
    val sqlMigrationSuffixes: String

    @ConfigCenterItem(
        key = "flyway.table",
        comment = "Flyway schema history 表名。",
        defaultValue = "flyway_schema_history",
    )
    val table: String

    @ConfigCenterItem(
        key = "flyway.target",
        comment = "Flyway 目标版本。",
        defaultValue = "latest",
    )
    val target: String

    @ConfigCenterItem(
        key = "flyway.validate-on-migrate",
        comment = "Flyway validateOnMigrate 开关。",
        defaultValue = "true",
    )
    val validateOnMigrate: Boolean
}
