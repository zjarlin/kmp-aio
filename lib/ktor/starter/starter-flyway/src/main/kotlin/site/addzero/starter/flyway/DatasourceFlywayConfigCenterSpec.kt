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
    )
    val enabled: Boolean

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.locations",
        comment = "数据源 {name} 的 Flyway 迁移脚本位置列表。",
    )
    val locations: String

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.baseline-on-migrate",
        comment = "是否在数据源 {name} 迁移时自动执行 Flyway 基线初始化。",
    )
    val baselineOnMigrate: Boolean

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.baseline-version",
        comment = "数据源 {name} 的 Flyway 基线版本号。",
    )
    val baselineVersion: String

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.clean-disabled",
        comment = "是否禁用数据源 {name} 的 Flyway clean 操作。",
    )
    val cleanDisabled: Boolean

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.connect-retries",
        comment = "数据源 {name} 的 Flyway 连接重试次数。",
    )
    val connectRetries: Int

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.group",
        comment = "是否将数据源 {name} 同一次运行中的待执行迁移分组提交。",
    )
    val group: Boolean

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.installed-by",
        comment = "数据源 {name} 的 Flyway 迁移记录安装人标识。",
    )
    val installedBy: String

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.mixed",
        comment = "是否允许数据源 {name} 的同一迁移中混用事务型与非事务型语句。",
    )
    val mixed: Boolean

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.out-of-order",
        comment = "是否允许数据源 {name} 乱序执行较旧版本的迁移脚本。",
    )
    val outOfOrder: Boolean

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.placeholder-replacement",
        comment = "是否启用数据源 {name} 的 Flyway 占位符替换。",
    )
    val placeholderReplacement: Boolean

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.placeholder-prefix",
        comment = "数据源 {name} 的 Flyway 占位符前缀。",
    )
    val placeholderPrefix: String

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.placeholder-suffix",
        comment = "数据源 {name} 的 Flyway 占位符后缀。",
    )
    val placeholderSuffix: String

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.placeholders.{placeholder}",
        comment = "数据源 {name} 的 Flyway 占位符 {placeholder}。",
    )
    val placeholderEntry: String

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.schemas",
        comment = "数据源 {name} 的 Flyway 管理 schema 列表。",
    )
    val schemas: String

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.skip-default-callbacks",
        comment = "是否跳过数据源 {name} 的 Flyway 内置回调。",
    )
    val skipDefaultCallbacks: Boolean

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.skip-default-resolvers",
        comment = "是否跳过数据源 {name} 的 Flyway 内置解析器。",
    )
    val skipDefaultResolvers: Boolean

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.sql-migration-prefix",
        comment = "数据源 {name} 的 Flyway SQL 迁移脚本文件名前缀。",
    )
    val sqlMigrationPrefix: String

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.sql-migration-separator",
        comment = "数据源 {name} 的 Flyway SQL 迁移脚本版本号与描述之间的分隔符。",
    )
    val sqlMigrationSeparator: String

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.sql-migration-suffixes",
        comment = "数据源 {name} 的 Flyway SQL 迁移脚本文件后缀列表。",
    )
    val sqlMigrationSuffixes: String

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.table",
        comment = "数据源 {name} 的 Flyway schema 历史表名称。",
    )
    val table: String

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.target",
        comment = "数据源 {name} 的 Flyway 目标版本。",
    )
    val target: String

    @ConfigCenterItem(
        key = "datasources.{name}.flyway.validate-on-migrate",
        comment = "是否在数据源 {name} 迁移前校验 Flyway 脚本。",
    )
    val validateOnMigrate: Boolean
}
