package site.addzero.coding.playground.server.generation

import org.koin.core.annotation.Single
import site.addzero.coding.playground.shared.dto.ContextAggregateDto
import site.addzero.coding.playground.shared.dto.GeneratedFileDto
import site.addzero.coding.playground.shared.dto.GenerationScaffoldMode
import site.addzero.coding.playground.shared.dto.GenerationTargetMetaDto
import site.addzero.coding.playground.shared.dto.ScaffoldPreset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.readText
import kotlin.io.path.writeText

@Single
class KcloudStyleScaffoldBootstrapper {
    fun detectMode(targetRoot: Path): GenerationScaffoldMode {
        return if (targetRoot.resolve("settings.gradle.kts").exists()) {
            GenerationScaffoldMode.EXISTING_ROOT
        } else {
            GenerationScaffoldMode.NEW_ROOT
        }
    }

    fun prepareRootScaffold(
        targetRoot: Path,
        target: GenerationTargetMetaDto,
        context: ContextAggregateDto,
    ): List<GeneratedFileDto> {
        if (target.scaffoldPreset != ScaffoldPreset.KCLOUD_STYLE) {
            return emptyList()
        }
        if (detectMode(targetRoot) != GenerationScaffoldMode.NEW_ROOT) {
            return emptyList()
        }

        val buildLogicPath = resolveBuildLogicPath(target)
        val repoRoot = buildLogicPath.parent?.parent
            ?: error("Unable to resolve repository root from build logic path '$buildLogicPath'")
        val versionCatalog = resolveVersionCatalog(repoRoot)
        val generatedFiles = mutableListOf<GeneratedFileDto>()

        targetRoot.createDirectories()
        targetRoot.resolve("gradle").createDirectories()

        generatedFiles += writeFile(
            targetRoot,
            "settings.gradle.kts",
            renderRootSettings(target, buildLogicPath),
            templateKey = "root-settings-gradle",
        )
        generatedFiles += writeFile(
            targetRoot,
            "build.gradle.kts",
            renderRootBuild(),
            templateKey = "root-build-gradle",
        )
        generatedFiles += writeFile(
            targetRoot,
            "composeApp/build.gradle.kts",
            renderComposeAppBuild(),
            templateKey = "root-compose-app-gradle",
        )
        generatedFiles += writeFile(
            targetRoot,
            "composeApp/src/jvmMain/kotlin/site/addzero/composeapp/Main.kt",
            renderComposeAppMain(context),
            templateKey = "root-compose-app-main",
        )

        val versionCatalogTarget = targetRoot.resolve("gradle/libs.versions.toml")
        versionCatalogTarget.parent?.createDirectories()
        Files.copy(versionCatalog, versionCatalogTarget, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
        generatedFiles += GeneratedFileDto(
            absolutePath = versionCatalogTarget.absolutePathString(),
            relativePath = targetRoot.relativize(versionCatalogTarget).toString(),
            templateKey = "root-version-catalog",
            content = versionCatalogTarget.readText(),
        )

        val gradlePropertiesSource = repoRoot.resolve("gradle.properties")
        if (gradlePropertiesSource.exists()) {
            val gradlePropertiesTarget = targetRoot.resolve("gradle.properties")
            Files.copy(gradlePropertiesSource, gradlePropertiesTarget, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
            generatedFiles += GeneratedFileDto(
                absolutePath = gradlePropertiesTarget.absolutePathString(),
                relativePath = targetRoot.relativize(gradlePropertiesTarget).toString(),
                templateKey = "root-gradle-properties",
                content = gradlePropertiesTarget.readText(),
            )
        }

        return generatedFiles
    }

    private fun resolveBuildLogicPath(target: GenerationTargetMetaDto): Path {
        val overridePath = target.variables["BUILD_LOGIC_PATH"]
            ?: System.getProperty("coding.playground.build.logic.path")
        if (!overridePath.isNullOrBlank()) {
            return Paths.get(overridePath).toAbsolutePath().normalize()
        }
        return locateRepoRoot()
            .resolve("checkouts/build-logic")
            .toAbsolutePath()
            .normalize()
    }

    private fun resolveVersionCatalog(repoRoot: Path): Path {
        return sequenceOf(
            repoRoot.resolve("gradle/libs.versions.toml"),
            repoRoot.resolve("checkouts/build-logic/gradle/libs.versions.toml"),
        ).firstOrNull { it.exists() }
            ?: error("Unable to locate libs.versions.toml from '$repoRoot'")
    }

    private fun locateRepoRoot(): Path {
        var current = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize()
        repeat(10) {
            if (current.resolve("settings.gradle.kts").exists() && current.resolve("checkouts").isDirectory()) {
                return current
            }
            current = current.parent ?: return@repeat
        }
        error("Unable to locate repository root from '${System.getProperty("user.dir")}'")
    }

    private fun renderRootSettings(target: GenerationTargetMetaDto, buildLogicPath: Path): String {
        val normalizedBuildLogicPath = buildLogicPath.toString().replace("\\", "/")
        return """
            rootProject.name = "${target.key.ifBlank { "coding-playground-generated" }}"
            enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

            pluginManagement {
                includeBuild("$normalizedBuildLogicPath")
                repositories {
                    gradlePluginPortal()
                    google()
                    mavenCentral()
                    maven("https://mirrors.tencent.com/nexus/repository/maven-public/")
                    maven("https://mirrors.tencent.com/nexus/repository/gradle-plugins/")
                }
            }

            dependencyResolutionManagement {
                repositoriesMode.set(org.gradle.api.initialization.resolve.RepositoriesMode.FAIL_ON_PROJECT_REPOS)
                repositories {
                    google()
                    mavenCentral()
                    maven("https://mirrors.tencent.com/nexus/repository/maven-public/")
                    maven("https://mirrors.tencent.com/nexus/repository/gradle-plugins/")
                }
            }

            include(":composeApp")
        """.trimIndent() + "\n"
    }

    private fun renderRootBuild(): String {
        return """
            plugins {
                alias(libs.plugins.kotlinMultiplatform) apply false
            }
        """.trimIndent() + "\n"
    }

    private fun renderComposeAppBuild(): String {
        return """
            plugins {
                id("site.addzero.buildlogic.kmp.cmp-aio")
            }
        """.trimIndent() + "\n"
    }

    private fun renderComposeAppMain(context: ContextAggregateDto): String {
        return """
            package site.addzero.composeapp

            import androidx.compose.material3.MaterialTheme
            import androidx.compose.material3.Text
            import androidx.compose.ui.window.Window
            import androidx.compose.ui.window.application

            fun main() = application {
                Window(
                    onCloseRequest = ::exitApplication,
                    title = "${context.context.name} Host",
                ) {
                    MaterialTheme {
                        Text("Generated host shell for ${context.context.name}")
                    }
                }
            }
        """.trimIndent() + "\n"
    }

    private fun writeFile(
        targetRoot: Path,
        relativePath: String,
        content: String,
        templateKey: String,
    ): GeneratedFileDto {
        val targetPath = targetRoot.resolve(relativePath)
        targetPath.parent?.createDirectories()
        targetPath.writeText(content)
        return GeneratedFileDto(
            absolutePath = targetPath.absolutePathString(),
            relativePath = targetRoot.relativize(targetPath).toString(),
            templateKey = templateKey,
            content = content,
        )
    }
}
