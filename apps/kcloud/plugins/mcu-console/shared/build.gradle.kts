@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-json")
    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
    id("site.addzero.buildlogic.kmp.kmp-datetime")
}
val libs = versionCatalogs.named("libs")

val generatedKspSourceDir = layout.buildDirectory.dir("generated/ksp/commonMain/kotlin")
val generatedContractSourceDir = layout.buildDirectory.dir("generated/source/codegen-context/commonMain/kotlin")
val addzeroLibJvmVersion: String by project
val generateMcuConsoleContractsTask = ":apps:kcloud:plugins:codegen-context:server:generateMcuConsoleContracts"
val generateMcuConsoleContractsEnabled =
    providers
        .gradleProperty("generateMcuConsoleContracts")
        .map { !it.equals("false", ignoreCase = true) }
        .orElse(true)
        .get()

kotlin {
    dependencies {
        implementation(libs.findLibrary("modbus-runtime").get())
    }
    sourceSets {
        commonMain {
            dependencies {
                kotlin.srcDir(generatedKspSourceDir)
                kotlin.srcDir(generatedContractSourceDir)
            }
        }
    }
}

tasks.configureEach {
    if (
        generateMcuConsoleContractsEnabled &&
        name in setOf(
            "compileCommonMainKotlinMetadata",
            "compileKotlinJvm",
            "jvmSourcesJar",
            "jvmJar",
        )
    ) {
        dependsOn(generateMcuConsoleContractsTask)
    }
}
