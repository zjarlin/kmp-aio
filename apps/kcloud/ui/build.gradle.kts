@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.bundling.Zip
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("site.addzero.buildlogic.kmp.cmp-app")
    id("site.addzero.buildlogic.kmp.cmp-wasm")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-json")
}

val libs = versionCatalogs.named("libs")
val addzeroLibJvmVersion: String by project
val desktopMainClass = "site.addzero.kcloud.bootstrap.MainKt"
val desktopRuntimeJavaLauncher = javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(24))
}

kotlin {
    dependencies {
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
    }
}

val stagedWasmReleaseDir = layout.buildDirectory.dir("dist/release/wasm")

val prepareWasmReleaseFiles = tasks.register<Sync>("prepareWasmReleaseFiles") {
    group = "distribution"
    description = "Stages the KCloud wasm browser distribution with a release entry page."
    dependsOn("wasmJsBrowserDistribution")
    from(layout.buildDirectory.dir("dist/wasmJs/productionExecutable"))
    from("release/wasm")
    into(stagedWasmReleaseDir)
}

tasks.register<Zip>("wasmDistZip") {
    group = "distribution"
    description = "Bundles the KCloud wasm browser distribution as dist.zip for GitHub Releases."
    dependsOn(prepareWasmReleaseFiles)
    from(stagedWasmReleaseDir)
    archiveFileName.set("dist.zip")
    destinationDirectory.set(layout.buildDirectory.dir("dist/release"))
}
