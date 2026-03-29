@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.gradle.api.tasks.JavaExec
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("site.addzero.buildlogic.kmp.cmp-app")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-json")
}

val libs = versionCatalogs.named("libs")
val desktopMainClass = "site.addzero.kcloud.MainKt"

kotlin {
    dependencies {
        implementation(project(":lib:compose:workbench-shell"))
        implementation(project(":apps:kcloud:shared"))
        // <managed:plugin-market-compose-deps:start>
        implementation(project(":apps:kcloud:plugins:mcu-console"))
        implementation(project(":apps:kcloud:plugins:system:config-center"))
        implementation(project(":apps:kcloud:plugins:system:ai-chat"))
        implementation(project(":apps:kcloud:plugins:system:knowledge-base"))
        implementation(project(":apps:kcloud:plugins:system:plugin-market"))
        implementation(project(":apps:kcloud:plugins:system:rbac"))
        implementation(project(":apps:kcloud:plugins:vibepocket"))
        // <managed:plugin-market-compose-deps:end>
        implementation(libs.findLibrary("site-addzero-compose-native-component-searchbar").get())
        implementation(libs.findLibrary("site-addzero-compose-native-component-tree").get())

    }
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":apps:kcloud:server"))
        }
    }
}

kotlin.jvm().mainRun {
    mainClass.set(desktopMainClass)
}

tasks.withType<JavaExec>().configureEach {
    if (name == "run" || name == "jvmRun" || name == "runJvm") {
        jvmArgs("-Dsun.java2d.metal=false")
    }
}

compose.desktop {
    application {
        mainClass = desktopMainClass
        jvmArgs += listOf(
            "-Dsun.java2d.metal=false",
        )
    }
}
