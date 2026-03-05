package site.addzero.vibepocket.routes

import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.koin.ktor.ext.inject
import site.addzero.ioc.annotation.Bean
import site.addzero.vibepocket.dto.OkResponse

/**
 * 从 app_config 表读取配置值
 */
fun KSqlClient.getConfig(key: String): String? {
    return createQuery(AppConfig::class) {
        where(table.key eq key)
        select(table.value)
    }.execute().firstOrNull()
}

/**
 * 写入或更新 app_config 配置
 *
 * AppConfig 的 key 字段标注了 @Key，Jimmer save 会自动 upsert。
 */
fun KSqlClient.setConfig(key: String, value: String, description: String? = null) {
    save(
        AppConfig {
            this.key = key
            this.value = value
            this.description = description
        }
    )
}


@Serializable
data class ConfigEntry(val key: String, val value: String, val description: String? = null)

@Serializable
data class ConfigResponse(val key: String, val value: String?)

@Serializable
data class StorageConfig(
    val type: String = "LOCAL",
    val endpoint: String? = null,
    val accessKey: String? = null,
    val secretKey: String? = null,
    val bucketName: String? = null,
    val region: String? = null,
    val domain: String? = null,
    val basePath: String? = null
)


@Bean
fun Route.configRoutes() {
    val sqlClient by inject<KSqlClient>()

    route("/api/config") {

        get("/{key}") {
            val key = call.parameters["key"]
                ?: throw IllegalArgumentException("key is required")
            val value = sqlClient.getConfig(key)
            call.respond(ConfigResponse(key = key, value = value))
        }

        put {
            val entry = call.receive<ConfigEntry>()
            sqlClient.setConfig(entry.key, entry.value, entry.description)
            call.respond(OkResponse())
        }

        route("/storage") {
            get {
                val config = StorageConfig(
                    type = sqlClient.getConfig("storage.type") ?: "LOCAL",
                    endpoint = sqlClient.getConfig("storage.endpoint"),
                    accessKey = sqlClient.getConfig("storage.accessKey"),
                    secretKey = sqlClient.getConfig("storage.secretKey"),
                    bucketName = sqlClient.getConfig("storage.bucketName"),
                    region = sqlClient.getConfig("storage.region"),
                    domain = sqlClient.getConfig("storage.domain"),
                    basePath = sqlClient.getConfig("storage.basePath")
                )
                call.respond(config)
            }

            put {
                val config = call.receive<StorageConfig>()
                sqlClient.setConfig("storage.type", config.type)
                config.endpoint?.let { sqlClient.setConfig("storage.endpoint", it) }
                config.accessKey?.let { sqlClient.setConfig("storage.accessKey", it) }
                config.secretKey?.let { sqlClient.setConfig("storage.secretKey", it) }
                config.bucketName?.let { sqlClient.setConfig("storage.bucketName", it) }
                config.region?.let { sqlClient.setConfig("storage.region", it) }
                config.domain?.let { sqlClient.setConfig("storage.domain", it) }
                config.basePath?.let { sqlClient.setConfig("storage.basePath", it) }
                call.respond(OkResponse())
            }
        }
    }
}
