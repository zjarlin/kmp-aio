@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.gradle.platform.base.internal.DefaultBinaryNamingScheme.component
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-datetime")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

val libs = versionCatalogs.named("libs")
val routeOwnerModuleDir =
    gradle.gradleUserHomeDir
        .resolve("addzero/route-owner/${rootProject.name}/apps-kcloud-ui/commonMain/kotlin")
        .absolutePath
val generatedKspSourceDir = layout.buildDirectory.dir("generated/ksp/commonMain/kotlin")
val generateHostConfigUiFormsTaskPath = ":apps:kcloud:plugins:host-config:server:kspKotlinJvm"

ksp {
    arg("routeGenPkg", "site.addzero.generated")
    arg("routeOwnerModule", routeOwnerModuleDir)
    arg("routeAggregationRole", "contributor")
    arg("routeModuleKey", project.parent!!.path)
}

dependencies {
    add("kspJvm", libs.findLibrary("site-addzero-route-processor").get())
}

kotlin {

    dependencies {
        api(project(":apps:kcloud:plugins:host-config:api"))
        implementation(project(":apps:kcloud:plugins:host-config:shared"))
        api(project(":apps:kcloud:plugins:mcu-console:api"))
        implementation(project(":apps:kcloud:plugins:mcu-console:shared"))
        implementation(libs.findLibrary("compose-cupertino-workbench").get())
        implementation(libs.findLibrary("site-addzero-compose-native-component-form").get())
        implementation(libs.findLibrary("site-addzero-compose-native-component-high-level").get())
        implementation(libs.findLibrary("site-addzero-compose-native-component-searchbar").get())
        implementation(libs.findLibrary("site-addzero-compose-native-component-tree").get())
        implementation(libs.findLibrary("site-addzero-route-core").get())
        implementation(libs.findLibrary("site-addzero-network-starter").get())
        implementation(libs.findLibrary("scaffold-spi").get())
        implementation(libs.findLibrary("io-github-robinpcrd-cupertino").get())
        implementation(libs.findLibrary("io-github-robinpcrd-cupertino-icons-extended").get())
        implementation(libs.findLibrary("site-addzero-compose-native-component-text").get())
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(generatedKspSourceDir)
        }
    }
}

tasks.matching { task ->
    task.name in setOf(
        "compileCommonMainKotlinMetadata",
        "compileKotlinJvm",
        "kspCommonMainKotlinMetadata",
        "kspKotlinJvm",
    )
}.configureEach {
    dependsOn(generateHostConfigUiFormsTaskPath)
}
