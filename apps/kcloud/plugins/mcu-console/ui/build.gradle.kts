@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("site.addzero.buildlogic.other.repositories-conventions")
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

val libs = versionCatalogs.named("libs")
//val addzeroLibJvmVersion: String by project
val routeOwnerModuleDir =
    gradle.gradleUserHomeDir
        .resolve("addzero/route-owner/${rootProject.name}/apps-kcloud-ui/commonMain/kotlin")
        .absolutePath
dependencies{
    add("kspJvm", libs.findLibrary("site-addzero-route-processor").get())
}

ksp {
    //route 上下文
    arg("routeGenPkg", "site.addzero.generated")
    arg("routeOwnerModule", routeOwnerModuleDir)
    arg("routeAggregationRole", "contributor")
    arg("routeModuleKey", project.parent!!.path)
}


kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":apps:kcloud:plugins:mcu-console:api"))
                implementation(project(":apps:kcloud:plugins:mcu-console:shared"))
                implementation(libs.findLibrary("compose-cupertino-workbench").get())
                implementation(libs.findLibrary("scaffold-spi").get())
                implementation(libs.findLibrary("site-addzero-route-core").get())
                implementation(libs.findLibrary("site-addzero-compose-native-component-button").get())
                implementation(libs.findLibrary("site-addzero-compose-native-component-text").get())
                implementation(libs.findLibrary("site-addzero-compose-native-component-searchbar").get())
            }
        }
    }
}
