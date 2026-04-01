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
