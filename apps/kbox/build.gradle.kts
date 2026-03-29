@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

import org.gradle.api.tasks.JavaExec
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    id("site.addzero.buildlogic.kmp.cmp-desktop")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-json")
}

val desktopMainClass = "site.addzero.kbox.MainKt"
val libs = versionCatalogs.named("libs")
val jdkVersion = libs.findVersion("jdk17").get().requiredVersion.toInt()
val desktopJavaLauncher = javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(jdkVersion))
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":lib:kbox-core"))
            implementation(project(":lib:kbox-ssh"))
        }
    }
}

kotlin.jvm().mainRun {
    mainClass.set(desktopMainClass)
}

tasks.withType<JavaExec>().configureEach {
    if (name == "jvmRun" || name == "runJvm") {
        javaLauncher.set(desktopJavaLauncher)
    }
}

compose.desktop {
    application {
        mainClass = desktopMainClass
    }
}
