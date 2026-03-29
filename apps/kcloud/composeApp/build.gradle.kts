@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("site.addzero.buildlogic.kmp.cmp-app")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-json")
    id("site.addzero.buildlogic.kmp.cmp-kcloud-aio")
}

val libs = versionCatalogs.named("libs")
val desktopMainClass = "site.addzero.kcloud.MainKt"

kotlin {
    dependencies {
        implementation(project(":lib:compose:workbench-shell"))
        implementation(project(":apps:kcloud:shared"))
        implementation(project(":apps:kcloud:plugins:system:shared"))
        implementation(libs.findLibrary("site-addzero-compose-native-component-searchbar").get())
        implementation(libs.findLibrary("site-addzero-compose-native-component-tree").get())

    }
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":apps:kcloud:server"))
            implementation(project(":apps:kcloud:plugins:system:shared"))
        }
        jvmTest.dependencies {
            implementation(project(":apps:kcloud:server"))
            implementation(project(":apps:kcloud:plugins:system:shared"))
            implementation(project(":apps:kcloud:plugins:vibepocket:server"))
            implementation(libs.findLibrary("io-ktor-ktor-server-core-jvm").get())
        }
    }
}

kotlin.jvm().mainRun {
    mainClass.set(desktopMainClass)
}

compose.desktop {
    application {
        mainClass = desktopMainClass
    }
}
