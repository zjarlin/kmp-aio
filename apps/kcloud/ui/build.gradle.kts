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

plugins {
    id("site.addzero.buildlogic.kmp.cmp-app-cupertino")
    id("site.addzero.buildlogic.kmp.cmp-wasm")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
}

val libs = versionCatalogs.named("libs")
val addzeroLibJvmVersion: String by project
val desktopMainClass = "site.addzero.kcloud.bootstrap.MainKt"
val wasmRuntimeConfigFileName = "kcloud-runtime-config.json"
val desktopDistributionName = providers.gradleProperty("desktopDistributionName")
    .orElse("")
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
            implementation(project(":lib:config-center"))
            implementation(project(":lib:ktor:starter:starter-spi"))
            implementation(libs.findLibrary("io-ktor-ktor-server-core-jvm").get())
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

val jvmMainCompilation = kotlin.jvm().compilations.getByName("main")
val wasmBrowserDistributionDir = layout.buildDirectory.dir("dist/wasmJs/productionExecutable")
val wasmReleaseDir = layout.buildDirectory.dir("dist/release/wasm")
val wasmRuntimeConfigOutput = layout.buildDirectory.file("generated/kcloud/wasm/$wasmRuntimeConfigFileName")

fun JavaExec.passOptionalGradlePropertyAsSystemProperty(
    systemKey: String,
    vararg gradleKeys: String,
) {
    val value = gradleKeys.firstNotNullOfOrNull { key ->
        providers.gradleProperty(key).orNull?.trim()?.takeIf(String::isNotBlank)
    } ?: return
    systemProperty(systemKey, value)
}

val exportWasmRuntimeConfig = tasks.register<JavaExec>("exportWasmRuntimeConfig") {
    group = "distribution"
    description = "Export KCloud Wasm public runtime config from config center."
    dependsOn(tasks.named("jvmMainClasses"))
    mainClass.set("site.addzero.kcloud.bootstrap.KcloudWasmRuntimeConfigExporterKt")
    classpath(
        jvmMainCompilation.output.allOutputs,
        jvmMainCompilation.runtimeDependencyFiles,
    )
    outputs.file(wasmRuntimeConfigOutput)
    args("--output=${wasmRuntimeConfigOutput.get().asFile.absolutePath}")
    val configPath = providers.gradleProperty("kcloudConfigPath").orNull
    if (!configPath.isNullOrBlank()) {
        args("--config=$configPath")
    }
    passOptionalGradlePropertyAsSystemProperty(
        systemKey = "ktor.environment",
        "kcloudKtorEnvironment",
        "ktor.environment",
    )
    passOptionalGradlePropertyAsSystemProperty(
        systemKey = "config-center.jdbc.url",
        "kcloudConfigCenterJdbcUrl",
        "config-center.jdbc.url",
    )
    passOptionalGradlePropertyAsSystemProperty(
        systemKey = "config-center.jdbc.user",
        "kcloudConfigCenterJdbcUser",
        "config-center.jdbc.user",
        "config-center.jdbc.username",
    )
    passOptionalGradlePropertyAsSystemProperty(
        systemKey = "config-center.jdbc.password",
        "kcloudConfigCenterJdbcPassword",
        "config-center.jdbc.password",
    )
    passOptionalGradlePropertyAsSystemProperty(
        systemKey = "config-center.jdbc.driver",
        "kcloudConfigCenterJdbcDriver",
        "config-center.jdbc.driver",
    )
    passOptionalGradlePropertyAsSystemProperty(
        systemKey = "config-center.jdbc.auto-ddl",
        "kcloudConfigCenterJdbcAutoDdl",
        "config-center.jdbc.auto-ddl",
    )
}

tasks.register<Sync>("prepareWasmReleaseFiles") {
    group = "distribution"
    description = "Assemble KCloud Wasm release files for Pages/static publishing."
    dependsOn(tasks.named("wasmJsBrowserDistribution"))
    dependsOn(exportWasmRuntimeConfig)
    into(wasmReleaseDir)
    from(wasmBrowserDistributionDir)
    from(layout.projectDirectory.dir("release/wasm")) {
        filter { line ->
            line.replace("__WASM_ENTRY_JS__", "composeApp.js")
        }
    }
    from(wasmRuntimeConfigOutput)
}

tasks.register<Zip>("wasmDistZip") {
    group = "distribution"
    description = "Package the prepared KCloud Wasm release files into dist.zip."
    dependsOn(tasks.named("prepareWasmReleaseFiles"))
    archiveFileName.set("dist.zip")
    destinationDirectory.set(layout.buildDirectory.dir("dist/release"))
    from(wasmReleaseDir)
}

compose.desktop {
    application {
        mainClass = desktopMainClass
        nativeDistributions {
            packageName = "OKMY DICS"
        }
    }
}
