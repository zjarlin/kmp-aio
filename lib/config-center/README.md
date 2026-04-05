# config-center

轻量级嵌入式配置中心。现在这套的定位已经比较明确了:

- `site.addzero:config-center`
  只负责通用契约、`ConfigCenterModule`、JDBC 存储、Koin 运行时装配。
- `lib/ktor/starter/starter-spi`
  负责 Ktor 启动期配置覆盖、管理页、启动公告等 Ktor 适配。

也就是说，`config-center` 核心模块现在不再耦合 Ktor。

## 现在到底怎么用

推荐直接记住 3 个入口:

1. `ConfigCenterJdbcSettings`
   你自己提供数据库连接信息。
2. `ConfigCenterBeanFactory.env(namespace, active)`
   你自己按应用创建一个运行时配置快照 bean。
3. `ApplicationConfig.withConfigCenterOverrides(namespace, active)`
   只有 Ktor 启动期要把配置中心值覆盖到 `ApplicationConfig` 时才用。

## 模块职责

`config-center` 核心模块提供:

- `ConfigCenterModule`
- `ConfigCenterBeanFactory`
- `ConfigCenterValueService` / `ConfigCenterAdminService`
- `JdbcConfigCenterValueService`
- `ConfigCenterRuntimeModule`
- `${otherKey}` 占位符解析
- JDBC 自动建表

不再提供:

- Ktor `ApplicationConfig` 包装层
- 环境变量读取入口
- `site.addzero.starter.ConfigCenterApplicationConfig` 这类兼容包装

## Koin 风格用法

推荐模式就是:

- 宿主直接提供一个自己应用的 `ConfigCenterModule` bean
- 其他地方直接注入 `ConfigCenterModule`
- 如果它是应用根配置，建议 `createdAtStart = true`，让依赖配置的 bean 不会先于它初始化

示例:

```kotlin
import site.addzero.configcenter.ConfigCenterModule
import site.addzero.configcenter.ConfigCenterBeanFactory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class ConfigCenterModule {
    @Single(createdAtStart = true)
    fun provideRuntimeEnv(
    ): ConfigCenterModule {
        return ConfigCenterBeanFactory.env(
            url = "jdbc:sqlite:./config-center.sqlite",
            namespace = "kcloud",
            active = "prod",
        )
    }
}
```

如果 JDBC 参数本身也想交给外部宿主控制，再退回两段式:

```kotlin
import site.addzero.configcenter.ConfigCenterBeanFactory
import site.addzero.configcenter.ConfigCenterModule
import site.addzero.configcenter.ConfigCenterJdbcSettings
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class ConfigCenterModule {
    @Single(createdAtStart = true)
    fun provideJdbcSettings(): ConfigCenterJdbcSettings {
        return ConfigCenterJdbcSettings(
            url = "jdbc:sqlite:./config-center.sqlite",
        )
    }

    @Single(createdAtStart = true)
    fun provideRuntimeEnv(
        jdbcSettings: ConfigCenterJdbcSettings,
    ): ConfigCenterModule {
        return ConfigCenterBeanFactory.env(
            settings = jdbcSettings,
            namespace = "kcloud",
            active = "prod",
        )
    }
}
```

消费时直接读:

```kotlin
class DemoSettings(
    private val env: ConfigCenterModule,
) {
    val serverHost: String =
        requireNotNull(env.string("server.host")) {
            "缺少配置 server.host"
        }

    val serverPort: Int =
        requireNotNull(env.int("server.port")) {
            "缺少配置 server.port"
        }
}
```

也可以先切路径:

```kotlin
val server = env.path("server")
val host = requireNotNull(server.string("host"))
val port = requireNotNull(server.int("port"))
```

## 不用 Koin 时

如果你只是想手动拿一个快照，现在可以直接一步初始化:

```kotlin
import site.addzero.configcenter.ConfigCenterBeanFactory

val env = ConfigCenterBeanFactory.env(
    url = "jdbc:sqlite:./config-center.sqlite",
    namespace = "kcloud",
    active = "prod",
)
```

如果你想复用同一个 factory，也可以:

```kotlin
import site.addzero.configcenter.ConfigCenterBeanFactory
import site.addzero.configcenter.ConfigCenterJdbcSettings

val factory = ConfigCenterBeanFactory.jdbc(
    ConfigCenterJdbcSettings(
        url = "jdbc:sqlite:./config-center.sqlite",
    ),
)

val env = factory.env(
    namespace = "kcloud",
    active = "prod",
)
```

## 值模型

当前存储层本质上只有字符串值。

推荐按下面理解:

- 字符串: 直接存
- 数字: 也直接按文本存，比如 `8080`
- 变量占位: 用 `${otherKey}`

例如:

```text
server.host = 127.0.0.1
server.port = 8080
server.base-url = http://${server.host}:${server.port}
```

运行时 `env(...)` 会先把同一个 `namespace + active` 下的整份快照读出来，然后做占位符展开。

注意:

- 引用了不存在的 key，会直接报错
- 出现循环引用，会直接报错
- `active` 不是 KSP 期裁剪，运行时按你传入的 `namespace + active` 读取并过滤

虽然 `ConfigCenterModule` 提供了 `boolean`、`list`、`map` 这类读取辅助，但这只是消费侧解析能力，不代表存储层存在独立类型系统。

## Ktor 现在怎么接

Ktor 相关能力已经迁到:

- `lib/ktor/starter/starter-spi/src/main/kotlin/site/addzero/configcenter`
- `lib/ktor/starter/starter-spi/src/main/kotlin/site/addzero/starter`

启动期覆盖配置:

```kotlin
import io.ktor.server.application.*
import site.addzero.configcenter.withConfigCenterOverrides

fun Application.module() {
    val effectiveConfig = environment.config.withConfigCenterOverrides(
        namespace = "kcloud",
        active = "prod",
    )

    // 后续把 effectiveConfig 交给你的启动流程
}
```

安装管理页:

```kotlin
import io.ktor.server.application.*
import site.addzero.starter.installConfigCenterAdminIfEnabled

fun Application.module() {
    installConfigCenterAdminIfEnabled(environment.config)
}
```

如果你不想走自动读取，也可以显式安装:

```kotlin
import io.ktor.server.application.*
import site.addzero.configcenter.ConfigCenterAdminSettings
import site.addzero.configcenter.ConfigCenterJdbcSettings
import site.addzero.configcenter.installConfigCenterAdmin

fun Application.module() {
    installConfigCenterAdmin(
        settings = ConfigCenterJdbcSettings(
            url = "jdbc:sqlite:./config-center.sqlite",
        ),
        adminSettings = ConfigCenterAdminSettings(
            enabled = true,
            path = "/config-center",
            title = "Config Center",
        ),
    )
}
```

## Ktor 侧 JDBC 配置来源

`starter-spi` 在读取 JDBC 设置时，支持两套来源:

1. 显式 `config-center.jdbc.*`
2. 兼容复用现有数据源:
   - `datasources.sqlite.*`
   - `datasources.postgres.*`

例如:

```hocon
config-center {
  enabled = true
  jdbc {
    url = "jdbc:sqlite:./config-center.sqlite"
    driver = "org.sqlite.JDBC"
    username = ""
    password = ""
    auto-ddl = true
  }
  admin {
    enabled = true
    path = "/config-center"
    title = "Config Center"
  }
}
```

## 启动覆盖的行为细节

`withConfigCenterOverrides(...)` 当前行为:

- 先读旧表结构 `config_center_secret/config_center_config/...`
- 再读当前表结构 `config_center_value`
- 如果旧表不存在，会自动忽略，不报错
- 当前表中的同名 key 会覆盖旧表值
- 最后统一做 `${otherKey}` 占位符解析

所以它既能兼容旧数据，又不会把历史表结构强绑定到新实现里。

## 关键 API

现在建议只认这些 API:

- `site.addzero.configcenter.ConfigCenterJdbcSettings`
- `site.addzero.configcenter.ConfigCenterBeanFactory`
- `site.addzero.configcenter.ConfigCenterModule`
- `site.addzero.configcenter.JdbcConfigCenterValueService`
- `site.addzero.configcenter.withConfigCenterOverrides`
- `site.addzero.starter.installConfigCenterAdminIfEnabled`
- `site.addzero.configcenter.installConfigCenterAdmin`

如果你在业务代码里还看到这些，说明可以继续清理:

- `site.addzero.starter.ConfigCenterApplicationConfig`
- 任何自己写的 config-center client/bootstrap 包装层
- 从环境变量兜底读取 config-center 参数的旧逻辑

## 运行时约束

- JDBC 存储是 JVM-only
- SQLite / PostgreSQL 都支持
- `ConfigCenterJdbcSettings.driver` 不传时会按 JDBC URL 推断
- `autoDdl = true` 时缺表会自动建表
- SQLite 不支持真正的持久表注释，所以表说明会落到 `config_center_meta`
