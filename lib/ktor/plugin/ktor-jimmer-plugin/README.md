# ktor-jimmer-plugin

`ktor-jimmer-plugin` 主要负责三件事：

- 按 `datasources.*` 配置创建 `DataSource`
- 为对应数据源创建默认 `KSqlClient`
- 给 `KSqlClient` 扩展原生 SQL、事务和连接方法，给业务侧补 Jimmer 不擅长表达的手写 SQL

当前内置支持的 JDBC 类型有：

- SQLite
- PostgreSQL
- MySQL / MariaDB

它不再内置任何业务表结构，也不会自动执行 `autoddl`。

## What This Library Does Not Own

- 不负责自动建表
- 不提供 `schema-sqlite.sql` / `schema-postgres.sql` / `schema-mysql.sql`
- 不声明任何业务表
- 不决定是否做 `autoddl`
- 不做业务侧的历史数据修正

这些都属于消费方，例如 `apps/kcloud/server` 或具体业务插件自己的 Flyway migration。

## Koin Wiring

推荐显式聚合，不要赌“第三方依赖里的 Koin 模块会不会自动被扫到”。

### 1. Root Koin Application

把库模块显式加到根 `@KoinApplication`：

```kotlin
@KoinApplication(
    modules = [
        site.addzero.kcloud.jimmer.di.JimmerKoinModule::class,
        MyServerJimmerKoinModule::class,
    ],
)
object MyServerKoinApplication
```

### 2. Pass `ApplicationConfig`

启动 Ktor/Koin 时，直接把 `ApplicationConfig` 作为类型化单例放进 Koin：

```kotlin
installKoin {
    withConfiguration<MyServerKoinApplication>()
    modules(
        module {
            single<ApplicationConfig> { environment.config }
        },
    )
}
```

## Schema Policy

业务表结构统一建议交给 Flyway：

- 建表、改表、补索引、历史 DDL 迁移放到 `db/migration/**`
- 运行时不要通过原生 SQL helper 或 `JimmerSqlScriptSupport` 手工执行 DDL
- 手写 SQL 统一通过 `KSqlClient` 扩展方法处理，例如 `queryForList`、`queryCount`、`executeUpdate`、`withTransaction`
- 如果某个迁移必须写原生 SQL，也应放进 Flyway migration，而不是塞回应用启动流程

辅助类：

- `site.addzero.kcloud.jimmer.di.DatabaseKt` 里的 `KSqlClient` 扩展
- `site.addzero.kcloud.jimmer.support.JimmerSqlScriptSupport`

其中 `JimmerSqlScriptSupport` 只适合执行已经明确受控的 SQL 片段，不应用来承担 schema 自动初始化。

## Resource Layout

```text
apps/my-server/src/jvmMain/resources/db/migration/mysql/V2026_04_08_001__init.sql
apps/my-plugin/server/src/jvmMain/resources/db/migration/mysql/V2026_04_08_101__feature_schema.sql
```

只要这些资源在运行时类路径上，Flyway 就会统一扫描并执行。

## Current KCloud Example

当前仓库里的落地方式：

- 库模块只保留 `JimmerKoinModule`、数据源 SPI 和 `KSqlClient` SQL helper
- `apps/kcloud/server` 提供 MySQL 数据源与 Flyway 配置
- `apps/kcloud/plugins/**/server/src/jvmMain/resources/db/migration/mysql/` 持有各插件自己的 schema 与演进脚本

也就是说，Jimmer 只负责数据源和 ORM，业务 schema 迁移统一归 Flyway，而不是 `ktor-jimmer-plugin`。
