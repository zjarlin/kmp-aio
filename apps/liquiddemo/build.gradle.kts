@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

import org.gradle.api.tasks.JavaExec
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    id("site.addzero.buildlogic.kmp.cmp-aio")
}

val desktopMainClass = "site.addzero.liquiddemo.MainKt"
val libs = versionCatalogs.named("libs")
val jdkVersion = libs.findVersion("jdk17").get().requiredVersion.toInt()
val desktopJavaLauncher = javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(jdkVersion))
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:compose:app-sidebar"))
            implementation(project(":lib:compose:workbench-shell"))
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
