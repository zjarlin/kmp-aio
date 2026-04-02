package site.addzero.starter.flyway

import site.addzero.configcenter.ConfigCenterItem
import site.addzero.configcenter.ConfigCenterNamespace

@ConfigCenterNamespace(
    namespace = "kcloud",
    objectName = "DatasourceFlywayConfigKeys",
)
interface DatasourceFlywayConfigCenterSpec {
    @ConfigCenterItem(
        key = "datasources.{name}.flyway.enabled",
        comment = "是否启用数据源 {name} 的 Flyway 自动迁移。",
        defaultValue = "true",
    )
    val enabled: Boolean

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.locations",
        comment = "数据源 {name} 的 Flyway 迁移脚本位置列表。",
    )
    val locations: String

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.baseline-on-migrate",
        comment = "数据源 {name} 的 Flyway baselineOnMigrate 开关。",
        defaultValue = "false",
    )
    val baselineOnMigrate: Boolean

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.baseline-version",
        comment = "数据源 {name} 的 Flyway baselineVersion。",
        defaultValue = "0",
    )
    val baselineVersion: String

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.clean-disabled",
        comment = "数据源 {name} 的 Flyway cleanDisabled 开关。",
        defaultValue = "true",
    )
    val cleanDisabled: Boolean

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.connect-retries",
        comment = "数据源 {name} 的 Flyway 连接重试次数。",
        defaultValue = "0",
    )
    val connectRetries: Int

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.group",
        comment = "数据源 {name} 的 Flyway group 开关。",
        defaultValue = "false",
    )
    val group: Boolean

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.installed-by",
        comment = "数据源 {name} 的 Flyway installedBy。",
    )
    val installedBy: String

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.mixed",
        comment = "数据源 {name} 的 Flyway mixed 开关。",
        defaultValue = "false",
    )
    val mixed: Boolean

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.out-of-order",
        comment = "数据源 {name} 的 Flyway outOfOrder 开关。",
        defaultValue = "false",
    )
    val outOfOrder: Boolean

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.placeholder-replacement",
        comment = "数据源 {name} 的 Flyway placeholderReplacement 开关。",
        defaultValue = "true",
    )
    val placeholderReplacement: Boolean

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.placeholder-prefix",
        comment = "数据源 {name} 的 Flyway placeholder 前缀。",
        defaultValue = "\${",
    )
    val placeholderPrefix: String

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.placeholder-suffix",
        comment = "数据源 {name} 的 Flyway placeholder 后缀。",
        defaultValue = "}",
    )
    val placeholderSuffix: String

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.placeholders.{placeholder}",
        comment = "数据源 {name} 的 Flyway 占位符 {placeholder}。",
    )
    val placeholderEntry: String

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.schemas",
        comment = "数据源 {name} 的 Flyway schemas 列表。",
    )
    val schemas: String

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.skip-default-callbacks",
        comment = "数据源 {name} 的 Flyway skipDefaultCallbacks 开关。",
        defaultValue = "false",
    )
    val skipDefaultCallbacks: Boolean

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.skip-default-resolvers",
        comment = "数据源 {name} 的 Flyway skipDefaultResolvers 开关。",
        defaultValue = "false",
    )
    val skipDefaultResolvers: Boolean

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.sql-migration-prefix",
        comment = "数据源 {name} 的 Flyway SQL migration prefix。",
        defaultValue = "V",
    )
    val sqlMigrationPrefix: String

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.sql-migration-separator",
        comment = "数据源 {name} 的 Flyway SQL migration separator。",
        defaultValue = "__",
    )
    val sqlMigrationSeparator: String

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.sql-migration-suffixes",
        comment = "数据源 {name} 的 Flyway SQL migration suffix 列表。",
    )
    val sqlMigrationSuffixes: String

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.table",
        comment = "数据源 {name} 的 Flyway schema history 表名。",
        defaultValue = "flyway_schema_history",
    )
    val table: String

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.target",
        comment = "数据源 {name} 的 Flyway 目标版本。",
        defaultValue = "latest",
    )
    val target: String

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.validate-on-migrate",
        comment = "数据源 {name} 的 Flyway validateOnMigrate 开关。",
        defaultValue = "true",
    )
    val validateOnMigrate: Boolean
}
