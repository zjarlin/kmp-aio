package site.addzero.kcloud.plugins.system.pluginmarket.jvm.service

import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.*
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginPackageAggregateDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginPackageDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginPresetKind
import site.addzero.kcloud.plugins.system.pluginmarket.service.PluginPresetService

@Single
class PluginPresetServiceImpl(
    private val sqlClient: KSqlClient,
    private val catalog: PluginMarketCatalogSupport,
    private val aggregateSupport: PluginPackageAggregateSupport,
) : PluginPresetService {
    override suspend fun applyPreset(packageId: String, presetKind: PluginPresetKind): PluginPackageAggregateDto {
        val pluginPackage = catalog.packageOrThrow(packageId)
        val now = catalog.now()
        val packageDto = aggregateSupport.packageDto(pluginPackage)
        val existingPaths = catalog.listFiles(packageId).associateBy { it.relativePath }
        presetFiles(packageDto, presetKind).forEachIndexed { index, (path, content, group) ->
            val file = existingPaths[path]
            val entity = new(PluginSourceFile::class).by {
                id = file?.id ?: catalog.newId()
                this.pluginPackage = catalog.packageRef(packageId)
                relativePath = path
                this.content = content
                contentHash = catalog.hashContent(content)
                fileGroup = group
                readOnly = false
                orderIndex = file?.orderIndex ?: index
                createdAt = file?.createdAt ?: now
                updatedAt = now
            }
            sqlClient.save(entity)
        }
        val binding = new(PluginPresetBinding::class).by {
            id = catalog.newId()
            this.pluginPackage = catalog.packageRef(packageId)
            this.presetKind = presetKind.name
            appliedAt = now
        }
        sqlClient.save(binding)
        return aggregateSupport.buildAggregate(packageId)
    }

    private fun presetFiles(
        pluginPackage: PluginPackageDto,
        presetKind: PluginPresetKind,
    ): List<PresetFile> {
        val packageRoot = pluginPackage.basePackage.appendSegment(pluginPackage.pluginId.toPascalCase().lowercase())
        val screenPackage = "$packageRoot.screen"
        val serverPackage = packageRoot
        val classPrefix = pluginPackage.pluginId.toPascalCase()
        val routePath = buildString {
            append(pluginPackage.pluginGroup?.replace("/", "/")?.let { "system/$it/" } ?: "system/")
            append(pluginPackage.pluginId)
        }.replace("system/system/", "system/")
        val title = when (presetKind) {
            PluginPresetKind.BLANK -> "空白插件"
            PluginPresetKind.TOOL -> "工具插件"
            PluginPresetKind.ADMIN -> "管理后台插件"
        }
        return listOf(
            PresetFile(
                "README.md",
                """
                # ${pluginPackage.name}

                ${pluginPackage.description ?: "由插件市场生成的 KCloud 插件模块。"}
                """.trimIndent(),
                "meta",
            ),
            PresetFile(
                "build.gradle.kts",
                buildGradleTemplate(),
                "meta",
            ),
            PresetFile(
                "src/commonMain/kotlin/${screenPackage.toPath()}/${classPrefix}Screen.kt",
                """
                package $screenPackage

                import androidx.compose.foundation.layout.Arrangement
                import androidx.compose.foundation.layout.Column
                import androidx.compose.foundation.layout.fillMaxSize
                import androidx.compose.foundation.layout.padding
                import site.addzero.cupertino.workbench.material3.MaterialTheme
                import site.addzero.cupertino.workbench.material3.Text
                import androidx.compose.runtime.Composable
                import androidx.compose.ui.Modifier
                import androidx.compose.ui.unit.dp
                import site.addzero.annotation.Route
                import site.addzero.annotation.RoutePlacement
                import site.addzero.annotation.RouteScene

                @Route(
                    title = "$title",
                    routePath = "$routePath",
                    icon = "Apps",
                    order = 50.0,
                    placement = RoutePlacement(
                        scene = RouteScene(
                            name = "${pluginPackage.name}",
                            icon = "Apps",
                            order = 500,
                        ),
                        defaultInScene = true,
                    ),
                )
                @Composable
                fun ${classPrefix}Screen() {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("${pluginPackage.name}", style = MaterialTheme.typography.titleMedium)
                        Text("$title 页面已经接入路由聚合。")
                    }
                }
                """.trimIndent(),
                "common",
            ),
            PresetFile(
                "src/jvmMain/kotlin/${serverPackage.toPath()}/${classPrefix}ServerKoinModule.kt",
                """
                package $serverPackage

                import org.koin.core.annotation.Module

                @Module
                class ${classPrefix}ServerKoinModule
                """.trimIndent(),
                "jvm",
            ),
            PresetFile(
                "src/jvmMain/kotlin/${serverPackage.toPath()}/${classPrefix}Routes.kt",
                """
                package $serverPackage

                import io.ktor.server.routing.Route
                import $serverPackage.routes.generated.springktor.registerGeneratedSpringRoutes

                /**
                 * ${pluginPackage.name} 服务端路由入口。
                 */
                fun Route.${pluginPackage.pluginId.camelCase()}Routes() {
                    registerGeneratedSpringRoutes()
                }
                """.trimIndent(),
                "jvm",
            ),
            PresetFile(
                "src/jvmMain/kotlin/${serverPackage.toPath()}/routes/${classPrefix}RouteHandlers.kt",
                """
                package $serverPackage.routes

                import org.springframework.web.bind.annotation.GetMapping

                /**
                 * ${pluginPackage.name} 健康检查路由。
                 */
                @GetMapping("/api/${pluginPackage.pluginId}/health")
                fun ${pluginPackage.pluginId.camelCase()}Health(): Map<String, String> {
                    return mapOf("pluginId" to "${pluginPackage.pluginId}", "status" to "ok")
                }
                """.trimIndent(),
                "jvm",
            ),
        )
    }

    private fun buildGradleTemplate(): String {
        return """
plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

val libs = versionCatalogs.named("libs")
val addzeroLibJvmVersion: String by project
val sharedSourceDir = project(":apps:kcloud:shared")
                .extensions
                .getByType<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>()
                .sourceSets
                .getByName("commonMain")
                .kotlin
                .srcDirs
                .first()
                .absolutePath
            val routeOwnerModuleDir = project(":apps:kcloud:ui")
                .extensions
                .getByType<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>()
                .sourceSets
                .getByName("commonMain")
                .kotlin
                .srcDirs
                .first()
                .absolutePath

            ksp {
                arg("sharedSourceDir", sharedSourceDir)
                arg("routeGenPkg", "site.addzero.generated")
                arg("routeOwnerModule", routeOwnerModuleDir)
                arg("routeModuleKey", project.path)
            }

            dependencies {
                add("kspCommonMainMetadata", libs.findLibrary("site-addzero-route-processor").get())
                add("kspJvm", libs.findLibrary("site-addzero-route-processor").get())
                add("kspJvm", libs.findLibrary("spring2ktor-server-processor").get())
            }

            kotlin {
                sourceSets {
                    commonMain.dependencies {
                        implementation("site.addzero:scaffold-spi:$addzeroLibJvmVersion")
                        implementation(libs.findLibrary("site-addzero-route-core").get())
                    }
                    jvmMain.dependencies {
                        implementation(libs.findLibrary("io-ktor-ktor-server-core-jvm").get())
                        implementation(libs.findLibrary("spring2ktor-server-core").get())
                        compileOnly(libs.findLibrary("org-springframework-spring-web").get())
                    }
                }
            }
        """.trimIndent()
    }
}
