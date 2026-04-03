package site.addzero.configcenter

import io.ktor.server.config.ApplicationConfig

object ConfigCenter {
    fun getEnv(
        config: ApplicationConfig,
    ): ConfigCenterEnv {
        return ConfigCenterEnv(
            stringReader = { key ->
                config.propertyOrNull(key)?.getString()
            },
            listReader = { key ->
                config.propertyOrNull(key)?.getList()
            },
            mapReader = { path ->
                runCatching {
                    val nestedConfig = config.config(path)
                    nestedConfig.keys().associateWith { nestedKey ->
                        nestedConfig.property(nestedKey).getString()
                    }
                }.getOrNull()
            },
            keysReader = { path ->
                runCatching {
                    config.config(path).toMap().keys
                }.getOrElse { emptySet() }
            },
        )
    }
}
