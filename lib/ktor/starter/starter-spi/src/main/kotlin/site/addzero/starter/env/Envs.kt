package site.addzero.starter.env

object Envs {
    object DatasourceConfig {
        val NAMESPACE = DatasourceConfigKeys.NAMESPACE

        fun enabled(name: String): String = DatasourceConfigKeys.enabled(name)

        fun url(name: String): String = DatasourceConfigKeys.url(name)

        fun driver(name: String): String = DatasourceConfigKeys.driver(name)

        fun user(name: String): String = DatasourceConfigKeys.user(name)

        fun password(name: String): String = DatasourceConfigKeys.password(name)
    }
}
