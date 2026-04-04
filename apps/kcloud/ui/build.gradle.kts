@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Zip
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import java.io.File

plugins {
    id("site.addzero.buildlogic.kmp.cmp-app-cupertino")
    id("site.addzero.buildlogic.kmp.cmp-wasm")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-json")
}

val libs = versionCatalogs.named("libs")
val addzeroLibJvmVersion: String by project
val desktopMainClass = "site.addzero.kcloud.bootstrap.MainKt"
val desktopDistributionName = providers.gradleProperty("desktopDistributionName")
    .orElse("okmy-dics")
    .get()
val desktopRuntimeJavaLauncher = javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(24))
}

kotlin {
    dependencies {
        implementation(project(":lib:compose:compose-cupertino-workbench"))
        implementation("site.addzero:scaffold-spi:$addzeroLibJvmVersion")
        implementation("site.addzero:compose-icon-map:2026.10329.10127")
        implementation(project(":apps:kcloud:shared"))
        implementation(project(":apps:kcloud:plugins:system:ai-chat:ui"))
        implementation(project(":apps:kcloud:plugins:system:config-center:ui"))
        implementation(project(":apps:kcloud:plugins:system:knowledge-base:ui"))
        implementation(project(":apps:kcloud:plugins:system:plugin-market:ui"))
        implementation(project(":apps:kcloud:plugins:system:rbac:ui"))
        implementation(project(":apps:kcloud:plugins:vibepocket:ui"))
        implementation(project(":lib:tool-kmp:network-starter"))
        implementation(project(":lib:compose:compose-native-component-chat"))
        implementation(libs.findLibrary("site-addzero-compose-native-component-searchbar").get())
        implementation(libs.findLibrary("site-addzero-compose-native-component-tree").get())
    }
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":apps:kcloud:server"))
            implementation(project(":apps:kcloud:plugins:mcu-console:ui"))
        }
        jvmTest.dependencies {
            implementation(project(":apps:kcloud:server"))
            implementation(project(":apps:kcloud:plugins:vibepocket:server"))
            implementation(libs.findLibrary("io-ktor-ktor-server-core-jvm").get())
        }
    }
}

kotlin.jvm().mainRun {
    mainClass.set(desktopMainClass)
}

tasks.withType<JavaExec>().configureEach {
    if (name == "jvmRun" || name == "runJvm") {
        javaLauncher.set(desktopRuntimeJavaLauncher)
    }
}

tasks.matching { task ->
    task.name in setOf("compileCommonMainKotlinMetadata", "compileKotlinJvm", "compileKotlinWasmJs")
}.configureEach {
    dependsOn(":apps:kcloud:shared:generateKCloudRouteArtifacts")
}

compose.desktop {
    application {
        buildTypes.release.proguard.isEnabled.set(false)
        mainClass = desktopMainClass
        javaHome = desktopRuntimeJavaLauncher.get().metadata.installationPath.asFile.absolutePath
        nativeDistributions {
            packageName = desktopDistributionName
        }
    }
}

val stagedWasmReleaseDir = layout.buildDirectory.dir("dist/release/wasm")
val wasmProductionExecutableDir = layout.buildDirectory.dir("dist/wasmJs/productionExecutable")
val wasmReleaseIndexTemplateFile = file("release/wasm/index.html")
val stagedWasmReleaseIndexFile = layout.buildDirectory.file("dist/release/wasm/index.html")

abstract class RenderWasmReleaseIndex : DefaultTask() {
    @get:InputFile
    abstract val templateFile: RegularFileProperty

    @get:InputDirectory
    abstract val entryDirectory: DirectoryProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun render() {
        val wasmEntryJs = entryDirectory.asFile.get()
            .listFiles()
            ?.map(File::getName)
            ?.singleOrNull { fileName ->
                fileName.endsWith(".js") &&
                    !fileName.endsWith(".js.map") &&
                    !fileName.endsWith(".LICENSE.txt")
            }
            ?: error("Unable to resolve a unique wasm JS entry in ${entryDirectory.asFile.get().absolutePath}")
        val renderedIndex = templateFile.asFile.get()
            .readText()
            .replace("__WASM_ENTRY_JS__", wasmEntryJs)
        outputFile.asFile.get().writeText(renderedIndex)
    }
}

val syncWasmReleaseFiles = tasks.register<Sync>("syncWasmReleaseFiles") {
    dependsOn("wasmJsBrowserDistribution")
    from(wasmProductionExecutableDir)
    into(stagedWasmReleaseDir)
}

val renderWasmReleaseIndex = tasks.register<RenderWasmReleaseIndex>("renderWasmReleaseIndex") {
    dependsOn(syncWasmReleaseFiles)
    templateFile.set(wasmReleaseIndexTemplateFile)
    entryDirectory.set(wasmProductionExecutableDir)
    outputFile.set(stagedWasmReleaseIndexFile)
}

val prepareWasmReleaseFiles = tasks.register("prepareWasmReleaseFiles") {
    group = "distribution"
    description = "Stages the KCloud wasm browser distribution with a release entry page."
    dependsOn(syncWasmReleaseFiles, renderWasmReleaseIndex)
}

tasks.register<Zip>("wasmDistZip") {
    group = "distribution"
    description = "Bundles the KCloud wasm browser distribution as dist.zip for GitHub Releases."
    dependsOn(prepareWasmReleaseFiles)
    from(stagedWasmReleaseDir)
    archiveFileName.set("dist.zip")
    destinationDirectory.set(layout.buildDirectory.dir("dist/release"))
}
