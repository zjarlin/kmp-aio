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
//val generatedApiSourceDir = layout.projectDirectory.dir("generated/commonMain/kotlin")
//val addzeroLibJvmVersion: String by project
val sharedSourceDir =
    project(":apps:kcloud:shared").extensions.getByType<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>().sourceSets.getByName(
        "commonMain"
    ).kotlin.srcDirs.first().absolutePath
val routeOwnerModuleDir =
    project(":apps:kcloud:ui").extensions.getByType<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>().sourceSets.getByName(
        "commonMain"
    ).kotlin.srcDirs.first().absolutePath
dependencies{
    kspCommonMainMetadata(libs.findLibrary("site-addzero-route-processor").get())
}

ksp {
    //route 上下文
    arg("sharedSourceDir", sharedSourceDir)
    arg("routeGenPkg", "site.addzero.generated")
    arg("routeOwnerModule", routeOwnerModuleDir)
    arg("routeModuleKey", project.parent!!.path)
}


kotlin {
    dependencies {
        implementation(project(":apps:kcloud:plugins:mcu-console:shared"))
        implementation(libs.findLibrary("compose-cupertino-workbench").get())
        implementation(libs.findLibrary("scaffold-spi").get())
        implementation(libs.findLibrary("site-addzero-route-core").get())
        implementation(libs.findLibrary("site-addzero-compose-native-component-button").get())
        implementation(libs.findLibrary("compose-native-component-text").get())
        implementation(libs.findLibrary("site-addzero-compose-native-component-searchbar").get())

    }
}
