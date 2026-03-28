@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

import org.gradle.api.tasks.JavaExec
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    id("site.addzero.buildlogic.kmp.cmp-app")
    id("site.addzero.buildlogic.kmp.kmp-ktor-server")
    id("site.addzero.buildlogic.kmp.kmp-konfig")
}

val desktopMainClass = "site.addzero.coding.playground.MainKt"
val libs = versionCatalogs.named("libs")
val jdkVersion = libs.findVersion("jdk17").get().requiredVersion.toInt()
val desktopJavaLauncher = javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(jdkVersion))
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":apps:coding-playground:shared"))
        }
        jvmMain.dependencies {
            implementation(project(":lib:ktor:plugin:ktor-jimmer-plugin"))
            implementation(project(":apps:coding-playground:server"))
        }
        jvmTest.dependencies {
            implementation(libs.findLibrary("org-babyfish-jimmer-jimmer-sql-kotlin").get())
            implementation(libs.findLibrary("org-xerial-sqlite-jdbc-v3").get())
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
