plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
    id("site.addzero.buildlogic.kmp.kmp-koin")
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
    add("kspJvm", libs.findLibrary("spring2ktor-server-processor").get())
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:compose:workbench-shell"))
            implementation(libs.findLibrary("site-addzero-route-core").get())
            implementation(libs.findLibrary("site-addzero-compose-native-component-button").get())
            implementation(libs.findLibrary("site-addzero-compose-native-component-searchbar").get())
        }
        jvmMain.dependencies {
            implementation(libs.findLibrary("io-ktor-ktor-server-core-jvm").get())
            implementation(libs.findLibrary("jserialcomm").get())
            implementation(libs.findLibrary("spring2ktor-server-core").get())
            compileOnly(libs.findLibrary("org-springframework-spring-web").get())
        }
    }
}
