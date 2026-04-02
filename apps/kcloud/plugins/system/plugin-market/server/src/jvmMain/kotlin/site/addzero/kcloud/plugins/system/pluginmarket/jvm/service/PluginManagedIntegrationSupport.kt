package site.addzero.kcloud.plugins.system.pluginmarket.jvm.service

import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.PluginPackage

@Single
class PluginManagedIntegrationSupport {
    fun renderManagedBlocks(dbPackages: List<PluginPackage>): ManagedIntegrationSnapshot {
        val fixedComposeDeps = listOf(
            """implementation(project(":apps:kcloud:plugins:mcu-console:ui"))""",
            """implementation(project(":apps:kcloud:plugins:system:ai-chat:ui"))""",
            """implementation(project(":apps:kcloud:plugins:system:config-center:ui"))""",
            """implementation(project(":apps:kcloud:plugins:system:knowledge-base:ui"))""",
            """implementation(project(":apps:kcloud:plugins:system:rbac:ui"))""",
            """implementation(project(":apps:kcloud:plugins:system:plugin-market:ui"))""",
            """implementation(project(":apps:kcloud:plugins:vibepocket:ui"))""",
        )
        val fixedServerDeps = listOf(
            """implementation(project(":apps:kcloud:plugins:mcu-console:server"))""",
            """implementation(project(":apps:kcloud:plugins:system:ai-chat:server"))""",
            """implementation(project(":apps:kcloud:plugins:system:config-center:server"))""",
            """implementation(project(":apps:kcloud:plugins:system:knowledge-base:server"))""",
            """implementation(project(":apps:kcloud:plugins:system:rbac:server"))""",
            """implementation(project(":apps:kcloud:plugins:system:plugin-market:server"))""",
            """implementation(project(":apps:kcloud:plugins:vibepocket:server"))""",
        )
        val fixedRouteTasks = listOf(
            """:apps:kcloud:plugins:mcu-console:ui:compileKotlinJvm""",
            """:apps:kcloud:plugins:system:ai-chat:ui:compileKotlinJvm""",
            """:apps:kcloud:plugins:system:config-center:ui:compileKotlinJvm""",
            """:apps:kcloud:plugins:system:knowledge-base:ui:compileKotlinJvm""",
            """:apps:kcloud:plugins:system:plugin-market:ui:compileKotlinJvm""",
            """:apps:kcloud:plugins:system:rbac:ui:compileKotlinJvm""",
            """:apps:kcloud:plugins:vibepocket:ui:compileKotlinJvm""",
        )
        val fixedComposeModules = listOf(
            "site.addzero.kcloud.bootstrap.KCloudUiScanKoinModule::class",
            "site.addzero.kcloud.bootstrap.KCloudUiSupportKoinModule::class",
        )
        val fixedDesktopModules = listOf(
            "site.addzero.kcloud.bootstrap.KCloudUiScanKoinModule::class",
            "site.addzero.kcloud.bootstrap.KCloudUiSupportKoinModule::class",
        )
        val fixedServerModules = listOf(
            "site.addzero.kcloud.server.KCloudServerScanKoinModule::class",
            "site.addzero.kcloud.jimmer.di.JimmerKoinModule::class",
            "site.addzero.kcloud.plugins.mcuconsole.McuConsoleServerKoinModule::class",
            "site.addzero.kcloud.plugins.system.configcenter.ConfigCenterServerKoinModule::class",
            "site.addzero.starter.banner.BannerStarterKoinModule::class",
            "site.addzero.starter.flyway.FlywayStarterKoinModule::class",
            "site.addzero.starter.openapi.OpenApiStarterKoinModule::class",
            "site.addzero.starter.serialization.SerializationStarterKoinModule::class",
            "site.addzero.starter.statuspages.StatusPagesStarterKoinModule::class",
        )
        val fixedRouteImports = listOf(
            "site.addzero.kcloud.plugins.system.configcenter.configCenterRoutes",
            "site.addzero.kcloud.plugins.mcuconsole.mcuConsoleRoutes",
            "site.addzero.kcloud.plugins.system.rbac.rbacRoutes",
            "site.addzero.kcloud.plugins.system.aichat.aiChatRoutes",
            "site.addzero.kcloud.plugins.system.knowledgebase.knowledgeBaseRoutes",
            "site.addzero.kcloud.plugins.system.pluginmarket.pluginMarketRoutes",
            "site.addzero.kcloud.vibepocket.routes.vibePocketRoutes",
        )
        val fixedRouteCalls = listOf(
            "configCenterRoutes()",
            "rbacRoutes()",
            "aiChatRoutes()",
            "knowledgeBaseRoutes()",
            "mcuConsoleRoutes()",
            "pluginMarketRoutes()",
            "vibePocketRoutes()",
        )

        val dynamicComposeDeps = dbPackages.map { pluginPackage ->
            """implementation(project("${gradlePathFor(pluginPackage)}"))"""
        }
        val dynamicServerDeps = dbPackages.filter { it.serverKoinModuleClass != null || it.routeRegistrarCall != null }
            .map { pluginPackage -> """implementation(project("${gradlePathFor(pluginPackage)}"))""" }
        val dynamicRouteTasks = dbPackages.map { pluginPackage ->
            """"${gradlePathFor(pluginPackage)}:compileKotlinJvm""""
        }
        val dynamicServerModules = dbPackages.mapNotNull { it.serverKoinModuleClass?.let { fqcn -> "$fqcn::class" } }
        val dynamicRouteImports = dbPackages.mapNotNull { it.routeRegistrarImport?.takeIf(String::isNotBlank) }
        val dynamicRouteCalls = dbPackages.mapNotNull { it.routeRegistrarCall?.takeIf(String::isNotBlank) }

        return ManagedIntegrationSnapshot(
            composeDependencies = (fixedComposeDeps + dynamicComposeDeps).distinct().sorted(),
            serverDependencies = (fixedServerDeps + dynamicServerDeps).distinct().sorted(),
            sharedRouteTasks = (fixedRouteTasks + dynamicRouteTasks).distinct().sorted(),
            composeModules = fixedComposeModules,
            desktopModules = fixedDesktopModules,
            serverModules = (fixedServerModules + dynamicServerModules).distinct(),
            serverRouteImports = (fixedRouteImports + dynamicRouteImports).distinct().sorted(),
            serverRouteCalls = (fixedRouteCalls + dynamicRouteCalls).distinct(),
        )
    }

    fun updateManagedIntegrationFiles(snapshot: ManagedIntegrationSnapshot): ManagedIntegrationResult {
        return ManagedIntegrationResult(
            changedFiles = emptyList(),
            diffText = buildString {
                appendLine("KCloud UI 壳层现在由 `apps/kcloud/ui/build.gradle.kts` 显式维护依赖，并由根 Koin 扫描 `site.addzero.kcloud`。")
                appendLine("新增插件 UI 后，需要同步补进 UI 模块依赖；正常情况下不再需要额外写 Compose Koin 模块。")
                appendLine()
                appendLine("Compose 依赖：")
                snapshot.composeDependencies.forEach { appendLine(it) }
                appendLine()
                appendLine("Server 依赖：")
                snapshot.serverDependencies.forEach { appendLine(it) }
                appendLine()
                appendLine("共享路由任务：")
                snapshot.sharedRouteTasks.forEach { appendLine(it) }
                appendLine()
                appendLine("Server 路由导入：")
                snapshot.serverRouteImports.forEach { appendLine(it) }
                appendLine()
                appendLine("Server 路由聚合：")
                snapshot.serverRouteCalls.forEach { appendLine(it) }
            },
        )
    }

    private fun gradlePathFor(pluginPackage: PluginPackage): String {
        val group = pluginPackage.pluginGroup?.split("/")?.filter { it.isNotBlank() }.orEmpty()
        return listOf(":apps", "kcloud", "plugins", *group.toTypedArray(), pluginPackage.pluginId)
            .joinToString(":")
    }
}

data class ManagedIntegrationSnapshot(
    val composeDependencies: List<String>,
    val serverDependencies: List<String>,
    val sharedRouteTasks: List<String>,
    val composeModules: List<String>,
    val desktopModules: List<String>,
    val serverModules: List<String>,
    val serverRouteImports: List<String>,
    val serverRouteCalls: List<String>,
)

data class ManagedIntegrationResult(
    val changedFiles: List<String>,
    val diffText: String,
)
