package site.addzero.starter.env

object Envs {
    object DatasourceConfig {
        fun enabled(name: String): String = "datasources.$name.enabled"

        fun url(name: String): String = "datasources.$name.url"

        fun driver(name: String): String = "datasources.$name.driver"

        fun user(name: String): String = "datasources.$name.user"

        fun password(name: String): String = "datasources.$name.password"
    }
}
