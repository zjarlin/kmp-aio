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
        comment = "是否在迁移时自动执行 Flyway 基线初始化。",
        defaultValue = "false",
    )
    val baselineOnMigrate: Boolean

    @ConfigCenterItem(
        key = "flyway.baseline-version",
        comment = "Flyway 基线版本号。",
        defaultValue = "0",
    )
    val baselineVersion: String

    @ConfigCenterItem(
        key = "flyway.clean-disabled",
        comment = "是否禁用 Flyway clean 操作。",
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
        comment = "是否将同一次运行中的待执行迁移分组提交。",
        defaultValue = "false",
    )
    val group: Boolean

    @ConfigCenterItem(
        key = "flyway.installed-by",
        comment = "Flyway 迁移记录中的安装人标识。",
    )
    val installedBy: String

    @ConfigCenterItem(
        key = "flyway.mixed",
        comment = "是否允许在同一迁移中混用事务型与非事务型语句。",
        defaultValue = "false",
    )
    val mixed: Boolean

    @ConfigCenterItem(
        key = "flyway.out-of-order",
        comment = "是否允许乱序执行较旧版本的迁移脚本。",
        defaultValue = "false",
    )
    val outOfOrder: Boolean

    @ConfigCenterItem(
        key = "flyway.placeholder-replacement",
        comment = "是否启用 Flyway 占位符替换。",
        defaultValue = "true",
    )
    val placeholderReplacement: Boolean

    @ConfigCenterItem(
        key = "flyway.placeholder-prefix",
        comment = "Flyway 占位符前缀。",
        defaultValue = "\${",
    )
    val placeholderPrefix: String

    @ConfigCenterItem(
        key = "flyway.placeholder-suffix",
        comment = "Flyway 占位符后缀。",
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
        comment = "Flyway 管理的 schema 列表。",
    )
    val schemas: String

    @ConfigCenterItem(
        key = "flyway.skip-default-callbacks",
        comment = "是否跳过 Flyway 内置回调。",
        defaultValue = "false",
    )
    val skipDefaultCallbacks: Boolean

    @ConfigCenterItem(
        key = "flyway.skip-default-resolvers",
        comment = "是否跳过 Flyway 内置解析器。",
        defaultValue = "false",
    )
    val skipDefaultResolvers: Boolean

    @ConfigCenterItem(
        key = "flyway.sql-migration-prefix",
        comment = "Flyway SQL 迁移脚本文件名前缀。",
        defaultValue = "V",
    )
    val sqlMigrationPrefix: String

    @ConfigCenterItem(
        key = "flyway.sql-migration-separator",
        comment = "Flyway SQL 迁移脚本版本号与描述之间的分隔符。",
        defaultValue = "__",
    )
    val sqlMigrationSeparator: String

    @ConfigCenterItem(
        key = "flyway.sql-migration-suffixes",
        comment = "Flyway SQL 迁移脚本文件后缀列表。",
    )
    val sqlMigrationSuffixes: String

    @ConfigCenterItem(
        key = "flyway.table",
        comment = "Flyway schema 历史表名称。",
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
        comment = "是否在迁移前校验 Flyway 脚本。",
        defaultValue = "true",
    )
    val validateOnMigrate: Boolean
}
