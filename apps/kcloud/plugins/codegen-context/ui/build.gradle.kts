@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

val libs = versionCatalogs.named("libs")
val generatedApiSourceDir = layout.projectDirectory.dir("generated/commonMain/kotlin")

val routeOwnerModuleDir =
    project(":apps:kcloud:ui")
        .extensions
        .getByType<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>()
        .sourceSets
        .getByName("commonMain")
        .kotlin
        .srcDirs
        .first()
        .absolutePath

val generateCodegenContextUiApisTaskPath = ":apps:kcloud:plugins:codegen-context:server:kspKotlinJvm"

ksp {
    arg("routeGenPkg", "site.addzero.generated")
    arg("routeOwnerModule", routeOwnerModuleDir)
    arg("routeAggregationRole", "contributor")
    arg("routeModuleKey", project.parent!!.path)
}

dependencies {
    kspCommonMainMetadata(libs.findLibrary("site-addzero-route-processor").get())
}

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir(generatedApiSourceDir)
            dependencies {
                implementation(project(":apps:kcloud:shared"))
                implementation(project(":apps:kcloud:plugins:codegen-context:shared"))
                implementation(libs.findLibrary("compose-cupertino-workbench").get())
                implementation(libs.findLibrary("site-addzero-route-core").get())
                implementation(libs.findLibrary("site-addzero-network-starter").get())
                implementation(libs.findLibrary("scaffold-spi").get())
                implementation(libs.findLibrary("io-github-robinpcrd-cupertino").get())
                implementation(libs.findLibrary("io-github-robinpcrd-cupertino-icons-extended").get())
            }
        }
    }
}

tasks.matching { task ->
    task.name in setOf(
        "kspCommonMainKotlinMetadata",
        "compileCommonMainKotlinMetadata",
        "compileKotlinJvm",
    )
}.configureEach {
    dependsOn(generateCodegenContextUiApisTaskPath)
}
