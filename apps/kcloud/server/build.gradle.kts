@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-server")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
}

val serverMainClass = "site.addzero.kcloud.ApplicationKt"

kotlin {
    dependencies {
        implementation(project(":apps:kcloud:plugins:scene-api"))
        implementation(project(":apps:kcloud:plugins:scenes:workspace:server"))
        implementation(project(":apps:kcloud:plugins:scenes:notes:server"))
        implementation(project(":apps:kcloud:plugins:scenes:second-brain:server"))
        implementation(project(":apps:kcloud:plugins:scenes:ops:server"))
        implementation(project(":apps:kcloud:plugins:scenes:system:server"))
    }
}

kotlin.jvm().mainRun {
    mainClass.set(serverMainClass)
}

tasks.named<JavaExec>("runJvm") {
    mainClass.set(serverMainClass)
}
