package site.addzero.starter.env

import site.addzero.starter.flyway.DatasourceFlywayConfigKeys as GeneratedDatasourceFlywayConfigKeys

object DatasourceFlywayEnvAccessor {
    val NAMESPACE = GeneratedDatasourceFlywayConfigKeys.NAMESPACE

    fun enabled(name: String): String = GeneratedDatasourceFlywayConfigKeys.enabled(name)

    fun locations(name: String): String = GeneratedDatasourceFlywayConfigKeys.locations(name)

    fun baselineOnMigrate(name: String): String = GeneratedDatasourceFlywayConfigKeys.baselineOnMigrate(name)

    fun baselineVersion(name: String): String = GeneratedDatasourceFlywayConfigKeys.baselineVersion(name)

    fun cleanDisabled(name: String): String = GeneratedDatasourceFlywayConfigKeys.cleanDisabled(name)

    fun connectRetries(name: String): String = GeneratedDatasourceFlywayConfigKeys.connectRetries(name)

    fun group(name: String): String = GeneratedDatasourceFlywayConfigKeys.group(name)

    fun installedBy(name: String): String = GeneratedDatasourceFlywayConfigKeys.installedBy(name)

    fun mixed(name: String): String = GeneratedDatasourceFlywayConfigKeys.mixed(name)

    fun outOfOrder(name: String): String = GeneratedDatasourceFlywayConfigKeys.outOfOrder(name)

    fun placeholderReplacement(name: String): String =
        GeneratedDatasourceFlywayConfigKeys.placeholderReplacement(name)

    fun placeholderPrefix(name: String): String = GeneratedDatasourceFlywayConfigKeys.placeholderPrefix(name)

    fun placeholderSuffix(name: String): String = GeneratedDatasourceFlywayConfigKeys.placeholderSuffix(name)

    fun placeholderEntry(
        name: String,
        placeholder: String,
    ): String = GeneratedDatasourceFlywayConfigKeys.placeholderEntry(name, placeholder)

    fun schemas(name: String): String = GeneratedDatasourceFlywayConfigKeys.schemas(name)

    fun skipDefaultCallbacks(name: String): String =
        GeneratedDatasourceFlywayConfigKeys.skipDefaultCallbacks(name)

    fun skipDefaultResolvers(name: String): String =
        GeneratedDatasourceFlywayConfigKeys.skipDefaultResolvers(name)

    fun sqlMigrationPrefix(name: String): String =
        GeneratedDatasourceFlywayConfigKeys.sqlMigrationPrefix(name)

    fun sqlMigrationSeparator(name: String): String =
        GeneratedDatasourceFlywayConfigKeys.sqlMigrationSeparator(name)

    fun sqlMigrationSuffixes(name: String): String =
        GeneratedDatasourceFlywayConfigKeys.sqlMigrationSuffixes(name)

    fun table(name: String): String = GeneratedDatasourceFlywayConfigKeys.table(name)

    fun target(name: String): String = GeneratedDatasourceFlywayConfigKeys.target(name)

    fun validateOnMigrate(name: String): String =
        GeneratedDatasourceFlywayConfigKeys.validateOnMigrate(name)
}

val Envs.DatasourceConfig.DatasourceFlywayConfigKeys
    get() = DatasourceFlywayEnvAccessor
