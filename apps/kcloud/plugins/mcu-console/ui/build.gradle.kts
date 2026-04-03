plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.ksp.ksp-jvm-cache-preparation")
    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
    id("site.addzero.buildlogic.kmp.kmp-koin")
}

val libs = versionCatalogs.named("libs")
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
        commonMain.dependencies {
            implementation(project(":apps:kcloud:design"))
            api(project(":apps:kcloud:plugins:mcu-console:shared"))
            implementation("site.addzero:scaffold-spi:$addzeroLibJvmVersion")
            implementation("io.github.alexzhirkevich:cupertino:0.1.0-alpha04")
            implementation("io.github.alexzhirkevich:cupertino-adaptive:0.1.0-alpha04")
            implementation(project(":lib:compose:shadcn-compose-component"))
            implementation(project(":lib:ksp:route:route-core"))
            implementation(libs.findLibrary("site-addzero-compose-native-component-button").get())
            implementation(libs.findLibrary("site-addzero-compose-native-component-searchbar").get())
            implementation(project(":lib:tool-kmp:network-starter"))
        }
    }
}
