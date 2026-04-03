package site.addzero.starter.env

import site.addzero.configcenter.ConfigCenterKeyDefinition

object Envs {
    object DatasourceConfig {
        val NAMESPACE: String = DatasourceConfigKeys.NAMESPACE

        fun enabled(name: String): String = DatasourceConfigKeys.enabled(name)

        fun enabledDefinition(name: String): ConfigCenterKeyDefinition = DatasourceConfigKeys.enabledDefinition(name)

        fun url(name: String): String = DatasourceConfigKeys.url(name)

        fun urlDefinition(name: String): ConfigCenterKeyDefinition = DatasourceConfigKeys.urlDefinition(name)

        fun driver(name: String): String = DatasourceConfigKeys.driver(name)

        fun driverDefinition(name: String): ConfigCenterKeyDefinition = DatasourceConfigKeys.driverDefinition(name)

        fun user(name: String): String = DatasourceConfigKeys.user(name)

        fun userDefinition(name: String): ConfigCenterKeyDefinition = DatasourceConfigKeys.userDefinition(name)

        fun password(name: String): String = DatasourceConfigKeys.password(name)

        fun passwordDefinition(name: String): ConfigCenterKeyDefinition = DatasourceConfigKeys.passwordDefinition(name)
    }
}
