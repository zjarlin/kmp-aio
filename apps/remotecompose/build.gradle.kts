@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

import org.gradle.api.tasks.JavaExec
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("site.addzero.buildlogic.kmp.cmp-aio")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
}

val libs = versionCatalogs.named("libs")
val desktopMainClass = "site.addzero.remotecompose.MainKt"
val ktorVersion = libs.findVersion("ktor").get().requiredVersion

kotlin {
    jvmToolchain(17)
    jvm {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":apps:remotecompose:client"))
        }
        jvmMain.dependencies {
            implementation(project(":apps:remotecompose:server"))
            implementation("io.ktor:ktor-server-cio-jvm:$ktorVersion")
            implementation(libs.findLibrary("io-ktor-ktor-server-content-negotiation").get())
            implementation(libs.findLibrary("io-ktor-ktor-serialization-kotlinx-json").get())
        }
    }
}

val java17Launcher = extensions.getByType<JavaToolchainService>().launcherFor {
    languageVersion.set(JavaLanguageVersion.of(17))
}

kotlin.jvm().mainRun {
    mainClass.set(desktopMainClass)
}

tasks.withType<JavaExec>().configureEach {
    javaLauncher.set(java17Launcher)
}

compose.desktop {
    application {
        mainClass = desktopMainClass
    }
}
