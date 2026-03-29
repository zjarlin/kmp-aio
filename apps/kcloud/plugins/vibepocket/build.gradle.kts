plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
    id("site.addzero.buildlogic.kmp.kmp-filekit")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
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
    add("kspJvm", libs.findLibrary("site-addzero-route-processor").get())
    add("kspJvm", libs.findLibrary("org-babyfish-jimmer-jimmer-ksp").get())
    add("kspJvm", "site.addzero:spring2ktor-server-processor:2026.03.13")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:compose:media-playlist-player"))
            implementation(project(":lib:compose:workbench-shell"))
            implementation(projects.lib.compose.liquidGlass)
            implementation(project(":lib:api:api-music-spi"))
            implementation(project(":lib:api:api-suno"))
            implementation(project(":lib:api:api-netease"))
            implementation(project(":lib:api:api-qqmusic"))
            implementation(libs.findLibrary("site-addzero-route-core").get())
        }
        jvmMain.dependencies {
            implementation(project(":lib:config-center:runtime-jvm"))
            implementation(libs.findLibrary("io-ktor-ktor-server-core-jvm").get())
            implementation(libs.findLibrary("org-babyfish-jimmer-jimmer-sql-kotlin").get())
            implementation("site.addzero:tool-api-suno:2026.02.06")
            implementation("com.squareup.okhttp3:okhttp:4.12.0")
            implementation("com.alibaba.fastjson2:fastjson2-kotlin:2.0.57")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
            implementation("org.unbescape:unbescape:1.1.6.RELEASE")
            implementation("javazoom:jlayer:1.0.1")
            implementation("io.ktor:ktor-client-core:2.3.7")
            implementation("io.ktor:ktor-client-cio:2.3.7")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
            implementation("io.ktor:ktor-client-logging:2.3.7")
            implementation("site.addzero:spring2ktor-server-core:2026.03.13")
            implementation(project(":lib:ktor:starter:starter-statuspages"))
            compileOnly(
                libs.findLibrary("org-springframework-spring-web").get()
            )
        }
    }
}
