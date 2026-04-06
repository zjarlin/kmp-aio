plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib-cupertino")
    // id("site.addzero.buildlogic.ksp.ksp-jvm-cache-preparation")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

val libs = versionCatalogs.named("libs")
val generatedApiSourceDir = layout.projectDirectory.dir("generated/commonMain/kotlin")
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
    arg("routeModuleKey", project.parent!!.path)
}

dependencies {
}

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir(generatedApiSourceDir)
        }
        commonMain.dependencies {
            implementation(libs.findLibrary("compose-cupertino-workbench").get())
            api(project(":apps:kcloud:plugins:mcu-console:shared"))
            implementation("site.addzero:scaffold-spi:$addzeroLibJvmVersion")
            implementation(libs.findLibrary("site-addzero-route-core").get())
            implementation(libs.findLibrary("site-addzero-compose-native-component-button").get())
            implementation("site.addzero:compose-native-component-text:$addzeroLibJvmVersion")
            implementation(libs.findLibrary("site-addzero-compose-native-component-searchbar").get())
        }
    }
}
