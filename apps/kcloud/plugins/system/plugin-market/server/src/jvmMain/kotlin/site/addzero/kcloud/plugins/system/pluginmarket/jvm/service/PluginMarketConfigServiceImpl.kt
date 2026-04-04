package site.addzero.kcloud.plugins.system.pluginmarket.jvm.service

import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.configcenter.spi.ConfigValueServiceSpi
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginMarketConfigDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.UpdatePluginMarketConfigRequest
import site.addzero.kcloud.plugins.system.pluginmarket.service.PluginMarketConfigService

private const val CONFIG_NAMESPACE = "kcloud-plugin-market"

@Single
class PluginMarketConfigServiceImpl(
    private val configValueService: ConfigValueServiceSpi,
) : PluginMarketConfigService {
    override suspend fun read(): PluginMarketConfigDto {
        ensureDefaults()
        return PluginMarketConfigDto(
            exportRootDir = readValue("export.rootDir") ?: "apps/kcloud/plugins",
            gradleCommand = readValue("gradle.command") ?: "./gradlew",
            gradleTasks = readValue("gradle.tasks")
                ?.split(" ")
                ?.map(String::trim)
                ?.filter(String::isNotBlank)
                ?: defaultGradleTasks(),
            javaHome = readValue("java.home"),
            environmentLines = readValue("environment.lines")
                ?.lineSequence()
                ?.map(String::trim)
                ?.filter(String::isNotBlank)
                ?.toList()
                ?: emptyList(),
            autoBuildEnabled = readValue("autoBuild.enabled")?.toBooleanStrictOrNull() ?: false,
        )
    }

    override suspend fun update(request: UpdatePluginMarketConfigRequest): PluginMarketConfigDto {
        saveValue("export.rootDir", request.exportRootDir, "插件源码导出根目录")
        saveValue("gradle.command", request.gradleCommand, "插件验证构建命令")
        saveValue("gradle.tasks", request.gradleTasks.joinToString(" "), "插件验证构建任务列表")
        saveValue("java.home", request.javaHome.orEmpty(), "插件验证构建 JAVA_HOME")
        saveValue("environment.lines", request.environmentLines.joinToString("\n"), "插件验证构建环境变量")
        saveValue("autoBuild.enabled", request.autoBuildEnabled.toString(), "插件导出后是否自动触发验证构建")
        return read()
    }

    private suspend fun ensureDefaults() {
        saveIfMissing("export.rootDir", "apps/kcloud/plugins", "插件源码导出根目录")
        saveIfMissing("gradle.command", "./gradlew", "插件验证构建命令")
        saveIfMissing("gradle.tasks", defaultGradleTasks().joinToString(" "), "插件验证构建任务列表")
        saveIfMissing("java.home", "", "插件验证构建 JAVA_HOME")
        saveIfMissing("environment.lines", "", "插件验证构建环境变量")
        saveIfMissing("autoBuild.enabled", "false", "插件导出后是否自动触发验证构建")
    }

    private suspend fun saveIfMissing(key: String, value: String, description: String) {
        if (readValue(key) == null) {
            saveValue(key, value, description)
        }
    }

    private suspend fun saveValue(key: String, value: String, _description: String) {
        configValueService.writeValue(
            namespace = CONFIG_NAMESPACE,
            key = key,
            value = value,
        )
    }

    private fun readValue(key: String): String? {
        return configValueService.readValue(CONFIG_NAMESPACE, key).value
    }

    private fun defaultGradleTasks(): List<String> {
        return listOf(
            ":apps:kcloud:shared:compileCommonMainKotlinMetadata",
            ":apps:kcloud:composeApp:compileKotlinJvm",
            ":apps:kcloud:server:compileKotlinJvm",
        )
    }
}
