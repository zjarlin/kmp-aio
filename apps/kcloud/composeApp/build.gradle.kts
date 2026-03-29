@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.gradle.api.tasks.JavaExec
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("site.addzero.buildlogic.kmp.cmp-app")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-json")
    id("site.addzero.buildlogic.kmp.cmp-kcloud-aio")
}

val libs = versionCatalogs.named("libs")
val desktopMainClass = "site.addzero.kcloud.MainKt"
val desktopRuntimeJavaLauncher = javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(24))
}

kotlin {
    dependencies {
        implementation(project(":lib:compose:scaffold-spi"))
        implementation(project(":apps:kcloud:shared"))
        // KCloud 插件 UI/root 模块由 cmp-kcloud-aio 自动扫描并注入到 commonMain，
        // 这里仅保留 composeApp 自身的基础依赖。
        implementation(libs.findLibrary("site-addzero-network-starter").get())
        implementation(libs.findLibrary("site-addzero-compose-icon-map").get())
        implementation(libs.findLibrary("site-addzero-compose-native-component-searchbar").get())
        implementation(libs.findLibrary("site-addzero-compose-native-component-tree").get())

    }
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":apps:kcloud:server"))
        }
        jvmTest.dependencies {
            implementation(project(":apps:kcloud:server"))
            implementation(project(":apps:kcloud:plugins:vibepocket:server"))
            implementation(libs.findLibrary("io-ktor-ktor-server-core-jvm").get())
        }
    }
}

kotlin.jvm().mainRun {
    mainClass.set(desktopMainClass)
}

tasks.withType<JavaExec>().configureEach {
    if (name == "jvmRun" || name == "runJvm") {
        javaLauncher.set(desktopRuntimeJavaLauncher)
    }
}

compose.desktop {
    application {
        mainClass = desktopMainClass
        javaHome = desktopRuntimeJavaLauncher.get().metadata.installationPath.asFile.absolutePath
    }
}
