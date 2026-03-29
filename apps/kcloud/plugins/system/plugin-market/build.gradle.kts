plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
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
    arg("routeModuleKey", project.path)
}

dependencies {
    add("kspCommonMainMetadata", libs.findLibrary("site-addzero-route-processor").get())
    add("kspJvm", libs.findLibrary("site-addzero-route-processor").get())
    add("kspJvm", libs.findLibrary("org-babyfish-jimmer-jimmer-ksp").get())
    add("kspJvm", libs.findLibrary("spring2ktor-server-processor").get())
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:compose:workbench-shell"))
            implementation(libs.findLibrary("site-addzero-route-core").get())
            implementation(libs.findLibrary("site-addzero-compose-native-component-button").get())
            implementation(libs.findLibrary("site-addzero-compose-native-component-searchbar").get())
            implementation(libs.findLibrary("site-addzero-compose-native-component-tree").get())
        }
        jvmMain.dependencies {
            implementation(project(":lib:config-center:runtime-jvm"))
            implementation(project(":lib:config-center:spec"))
            implementation(libs.findLibrary("io-ktor-ktor-server-core-jvm").get())
            implementation(libs.findLibrary("org-babyfish-jimmer-jimmer-sql-kotlin").get())
            implementation(libs.findLibrary("spring2ktor-server-core").get())
            compileOnly(libs.findLibrary("org-springframework-spring-web").get())
        }
    }
}

tasks.matching { task ->
    task.name == "kspKotlinJvm"
}.configureEach {
    doFirst {
        delete(layout.buildDirectory.dir("kspCaches/jvm/jvmMain/symbolLookups"))
        layout.buildDirectory.dir("kspCaches/jvm/jvmMain/symbols").get().asFile.mkdirs()
        layout.buildDirectory.dir("kspCaches/jvm/jvmMain/sourceToOutputs").get().asFile.mkdirs()
        layout.buildDirectory.dir("kspCaches/jvm/jvmMain/classpath").get().asFile.mkdirs()
    }
}
