@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

import org.gradle.api.tasks.JavaExec
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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

val desktopMainClass = "site.addzero.vibepocket.MainKt"

dependencies {
}

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
            implementation(project(":apps:vibepocket:shared"))
            implementation(project(":apps:kcloud:features:feature-api"))
            implementation(project(":lib:media-playlist-player"))
            implementation(project(":lib:vibepocket-ui"))
            implementation(project(":lib:compose:app-sidebar"))
            implementation(projects.lib.compose.liquidGlass)
            implementation(project(":lib:api-music-spi"))
            implementation(project(":lib:api-suno"))
        }
        jvmMain.dependencies {
            implementation(project(":apps:vibepocket:server"))
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
