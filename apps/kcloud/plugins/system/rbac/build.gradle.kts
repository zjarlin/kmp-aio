plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-json")
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
            implementation(libs.findLibrary("site-addzero-route-core").get())
        }
    }
}
