plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

val libs = versionCatalogs.named("libs")
val sharedSourceDir = project(":apps:kcloud:shared")
    .extensions
    .getByType<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>()
    .sourceSets
    .getByName("commonMain")
    .kotlin
    .srcDirs
    .first()
    .absolutePath
val routeOwnerModuleDir = project(":apps:kcloud:composeApp")
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
}

dependencies {
    add("kspCommonMainMetadata", libs.findLibrary("site-addzero-route-processor").get())
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:compose:workbench-shell"))
            implementation(project(":lib:config-center:client"))
            implementation(libs.findLibrary("site-addzero-route-core").get())
            implementation(libs.findLibrary("site-addzero-compose-native-component-button").get())
            implementation(libs.findLibrary("site-addzero-compose-native-component-searchbar").get())
        }
    }
}
