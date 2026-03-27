@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion

/**
 * VibePocket 应用模块 - KMP Compose Multiplatform 桌面应用
 *
 * 复制此模块创建新应用:
 * 1. 复制 apps/vibepocket 到 apps/{your-app-name}
 * 2. 修改 namespace 和 artifact
 * 3. 更新依赖
 */
plugins {
    id("site.addzero.buildlogic.kmp.cmp-aio")
}

val libs = versionCatalogs.named("libs")

kotlin {
    dependencies {
        implementation(project(":lib:compose:workbench-shell"))
    }

//    sourceSets {
//        jvmMain.dependencies {
//            implementation(libs.findLibrary("io-ktor-ktor-server-core-jvm").get())
//            implementation(libs.findLibrary("io-ktor-ktor-server-content-negotiation").get())
//            implementation(libs.findLibrary("io-ktor-ktor-serialization-kotlinx-json").get())
//            implementation(libs.findLibrary("io-ktor-ktor-server-netty-jvm").get())
//        }
//    }
}
