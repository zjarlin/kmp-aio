plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

val libs = versionCatalogs.named("libs")
val sharedSourceDir = project(":apps:kbox:shared")
    .extensions
    .getByType<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>()
    .sourceSets
    .getByName("commonMain")
    .kotlin
    .srcDirs
    .first()
    .absolutePath
val routeOwnerModuleDir = project(":apps:kbox:composeApp")
    .extensions
    .getByType<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>()
    .sourceSets
    .getByName("jvmMain")
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
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:compose:scaffold-spi"))
            implementation(libs.findLibrary("site-addzero-route-core").get())
        }
        jvmMain.dependencies {
            implementation(project(":lib:kbox-core"))
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
