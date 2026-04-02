# config-center

Lightweight embedded config center for KMP projects.

- Maven coordinate: `site.addzero:config-center`
- Local module path: `/Users/zjarlin/IdeaProjects/kmp-aio/lib/config-center`
- Module shape: KMP contract in `commonMain`, JDBC storage and embedded admin page in `jvmMain`

## What It Provides

- `ApplicationConfig` override loading from JDBC-backed config values
- Automatic schema creation when the config-center table is missing
- Simple embedded H5 admin page for listing, editing, and deleting config values
- Lightweight CRUD service without Jimmer or Flyway
- Koin startup announcement in Chinese so operators can quickly find the embedded H5 admin page

## Minimal Usage

```kotlin
import io.ktor.server.application.*
import site.addzero.configcenter.ConfigCenterJdbcSettings
import site.addzero.configcenter.installConfigCenterAdmin
import site.addzero.configcenter.withConfigCenterOverrides

fun Application.module() {
    val effectiveConfig = environment.config.withConfigCenterOverrides(
        namespace = "demo-app",
        active = "dev",
    )

    installConfigCenterAdmin(
        settings = ConfigCenterJdbcSettings(
            url = "jdbc:sqlite:demo-config-center.sqlite",
        ),
    )
}
```

## Koin Startup Hint

If the host app uses the repository's Koin compiler configuration, this library now registers a startup singleton that prints a Chinese startup hint for the embedded H5 admin page.

Example output:

```text
配置中心管理页已启用。可通过 H5 页面管理配置命名空间、环境、键值和说明等配置元数据。访问入口：http://127.0.0.1:8080/config-center
```

## Config Keys

The JDBC runtime can be built from either:

- `config-center.jdbc.*`
- or the existing `datasources.sqlite.*` / `datasources.postgres.*` settings

Supported `config-center` keys:

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

## Runtime Constraints

- JDBC storage is JVM-only by design
- Table comments are applied for PostgreSQL
- SQLite does not support persistent table comments, so the library writes the same note into `config_center_meta`
