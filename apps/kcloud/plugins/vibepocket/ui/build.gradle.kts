plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.ksp.ksp-jvm-cache-preparation")
    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
    id("site.addzero.buildlogic.kmp.kmp-filekit")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
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
    add("kspCommonMainMetadata", project(":lib:ksp:route:route-processor"))
    add("kspJvm", project(":lib:ksp:route:route-processor"))
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":apps:kcloud:plugins:vibepocket"))
            implementation(project(":lib:compose:media-playlist-player"))
            implementation(project(":lib:api:api-music-spi"))
            implementation(project(":lib:api:api-suno"))
            implementation(project(":lib:api:api-netease"))
            implementation(project(":lib:api:api-qqmusic"))
            implementation(projects.lib.compose.liquidGlass)
            implementation(project(":lib:ksp:route:route-core"))
            implementation(project(":lib:tool-kmp:network-starter"))
        }
    }
}
