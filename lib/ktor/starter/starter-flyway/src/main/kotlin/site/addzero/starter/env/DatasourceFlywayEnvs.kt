package site.addzero.starter.env

import site.addzero.configcenter.ConfigCenterKeyDefinition
import site.addzero.starter.flyway.DatasourceFlywayConfigKeys as GeneratedDatasourceFlywayConfigKeys

object DatasourceFlywayEnvAccessor {
    val NAMESPACE = GeneratedDatasourceFlywayConfigKeys.NAMESPACE

    fun enabled(name: String): String = GeneratedDatasourceFlywayConfigKeys.enabled(name)

    fun enabledDefinition(name: String): ConfigCenterKeyDefinition =
        GeneratedDatasourceFlywayConfigKeys.enabledDefinition(name)

    fun locations(name: String): String = GeneratedDatasourceFlywayConfigKeys.locations(name)

    fun locationsDefinition(name: String): ConfigCenterKeyDefinition =
        GeneratedDatasourceFlywayConfigKeys.locationsDefinition(name)

    fun baselineOnMigrate(name: String): String = GeneratedDatasourceFlywayConfigKeys.baselineOnMigrate(name)

    fun baselineOnMigrateDefinition(name: String): ConfigCenterKeyDefinition =
        GeneratedDatasourceFlywayConfigKeys.baselineOnMigrateDefinition(name)

    fun baselineVersion(name: String): String = GeneratedDatasourceFlywayConfigKeys.baselineVersion(name)

    fun baselineVersionDefinition(name: String): ConfigCenterKeyDefinition =
        GeneratedDatasourceFlywayConfigKeys.baselineVersionDefinition(name)

    fun cleanDisabled(name: String): String = GeneratedDatasourceFlywayConfigKeys.cleanDisabled(name)

    fun cleanDisabledDefinition(name: String): ConfigCenterKeyDefinition =
        GeneratedDatasourceFlywayConfigKeys.cleanDisabledDefinition(name)

    fun connectRetries(name: String): String = GeneratedDatasourceFlywayConfigKeys.connectRetries(name)

    fun connectRetriesDefinition(name: String): ConfigCenterKeyDefinition =
        GeneratedDatasourceFlywayConfigKeys.connectRetriesDefinition(name)

    fun group(name: String): String = GeneratedDatasourceFlywayConfigKeys.group(name)

    fun groupDefinition(name: String): ConfigCenterKeyDefinition =
        GeneratedDatasourceFlywayConfigKeys.groupDefinition(name)

    fun installedBy(name: String): String = GeneratedDatasourceFlywayConfigKeys.installedBy(name)

    fun installedByDefinition(name: String): ConfigCenterKeyDefinition =
        GeneratedDatasourceFlywayConfigKeys.installedByDefinition(name)

    fun mixed(name: String): String = GeneratedDatasourceFlywayConfigKeys.mixed(name)

    fun mixedDefinition(name: String): ConfigCenterKeyDefinition =
        GeneratedDatasourceFlywayConfigKeys.mixedDefinition(name)

    fun outOfOrder(name: String): String = GeneratedDatasourceFlywayConfigKeys.outOfOrder(name)

    fun outOfOrderDefinition(name: String): ConfigCenterKeyDefinition =
        GeneratedDatasourceFlywayConfigKeys.outOfOrderDefinition(name)

    fun placeholderReplacement(name: String): String =
        GeneratedDatasourceFlywayConfigKeys.placeholderReplacement(name)

    fun placeholderReplacementDefinition(name: String): ConfigCenterKeyDefinition =
        GeneratedDatasourceFlywayConfigKeys.placeholderReplacementDefinition(name)

    fun placeholderPrefix(name: String): String = GeneratedDatasourceFlywayConfigKeys.placeholderPrefix(name)

    fun placeholderPrefixDefinition(name: String): ConfigCenterKeyDefinition =
        GeneratedDatasourceFlywayConfigKeys.placeholderPrefixDefinition(name)

    fun placeholderSuffix(name: String): String = GeneratedDatasourceFlywayConfigKeys.placeholderSuffix(name)

    fun placeholderSuffixDefinition(name: String): ConfigCenterKeyDefinition =
        GeneratedDatasourceFlywayConfigKeys.placeholderSuffixDefinition(name)

    fun placeholderEntry(
        name: String,
        placeholder: String,
    ): String = GeneratedDatasourceFlywayConfigKeys.placeholderEntry(name, placeholder)

    fun placeholderEntryDefinition(
        name: String,
        placeholder: String,
    ): ConfigCenterKeyDefinition =
        GeneratedDatasourceFlywayConfigKeys.placeholderEntryDefinition(name, placeholder)

    fun schemas(name: String): String = GeneratedDatasourceFlywayConfigKeys.schemas(name)

    fun schemasDefinition(name: String): ConfigCenterKeyDefinition =
        GeneratedDatasourceFlywayConfigKeys.schemasDefinition(name)

    fun skipDefaultCallbacks(name: String): String =
        GeneratedDatasourceFlywayConfigKeys.skipDefaultCallbacks(name)

    fun skipDefaultCallbacksDefinition(name: String): ConfigCenterKeyDefinition =
        GeneratedDatasourceFlywayConfigKeys.skipDefaultCallbacksDefinition(name)

    fun skipDefaultResolvers(name: String): String =
        GeneratedDatasourceFlywayConfigKeys.skipDefaultResolvers(name)

    fun skipDefaultResolversDefinition(name: String): ConfigCenterKeyDefinition =
        GeneratedDatasourceFlywayConfigKeys.skipDefaultResolversDefinition(name)

    fun sqlMigrationPrefix(name: String): String =
        GeneratedDatasourceFlywayConfigKeys.sqlMigrationPrefix(name)

    fun sqlMigrationPrefixDefinition(name: String): ConfigCenterKeyDefinition =
        GeneratedDatasourceFlywayConfigKeys.sqlMigrationPrefixDefinition(name)

    fun sqlMigrationSeparator(name: String): String =
        GeneratedDatasourceFlywayConfigKeys.sqlMigrationSeparator(name)

    fun sqlMigrationSeparatorDefinition(name: String): ConfigCenterKeyDefinition =
        GeneratedDatasourceFlywayConfigKeys.sqlMigrationSeparatorDefinition(name)

    fun sqlMigrationSuffixes(name: String): String =
        GeneratedDatasourceFlywayConfigKeys.sqlMigrationSuffixes(name)

    fun sqlMigrationSuffixesDefinition(name: String): ConfigCenterKeyDefinition =
        GeneratedDatasourceFlywayConfigKeys.sqlMigrationSuffixesDefinition(name)

    fun table(name: String): String = GeneratedDatasourceFlywayConfigKeys.table(name)

    fun tableDefinition(name: String): ConfigCenterKeyDefinition =
        GeneratedDatasourceFlywayConfigKeys.tableDefinition(name)

    fun target(name: String): String = GeneratedDatasourceFlywayConfigKeys.target(name)

    fun targetDefinition(name: String): ConfigCenterKeyDefinition =
        GeneratedDatasourceFlywayConfigKeys.targetDefinition(name)

    fun validateOnMigrate(name: String): String =
        GeneratedDatasourceFlywayConfigKeys.validateOnMigrate(name)

    fun validateOnMigrateDefinition(name: String): ConfigCenterKeyDefinition =
        GeneratedDatasourceFlywayConfigKeys.validateOnMigrateDefinition(name)
}

val Envs.DatasourceConfig.DatasourceFlywayConfigKeys
    get() = DatasourceFlywayEnvAccessor
