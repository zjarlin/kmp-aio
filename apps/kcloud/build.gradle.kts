@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

import org.gradle.api.tasks.JavaExec
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * KCloud - 类 Nextcloud 的跨平台同步客户端
 *
 * 支持 WebDAV/S3/SSH 多种存储后端，端到端加密
 */
plugins {
    id("site.addzero.buildlogic.kmp.cmp-aio")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
}

val desktopMainClass = "com.kcloud.MainKt"
val libs = versionCatalogs.named("libs")
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
            implementation(project(":apps:kcloud:plugins:ai"))
            implementation(project(":apps:kcloud:plugins:desktop-integration-plugin"))
            implementation(project(":apps:kcloud:plugins:dotfiles"))
            implementation(project(":apps:kcloud:plugins:environment"))
            implementation(project(":apps:kcloud:plugins:file"))
            implementation(project(":apps:kcloud:plugins:notes"))
            implementation(project(":apps:kcloud:plugins:plugin-api"))
            implementation(project(":apps:kcloud:plugins:package-organizer"))
            implementation(project(":apps:kcloud:plugins:quick-transfer"))
            implementation(project(":apps:kcloud:plugins:settings-plugin"))
            implementation(project(":apps:kcloud:plugins:server-management"))
            implementation(project(":apps:kcloud:plugins:ssh"))
            implementation(project(":apps:kcloud:plugins:transfer-history"))
            implementation(project(":apps:kcloud:plugins:webdav"))
        }
        jvmMain.dependencies {
            implementation("io.ktor:ktor-server-cio-jvm:$ktorVersion")
            implementation(libs.findLibrary("io-ktor-ktor-server-content-negotiation").get())
            implementation(libs.findLibrary("io-ktor-ktor-serialization-kotlinx-json").get())
            implementation("io.ktor:ktor-server-cors-jvm:$ktorVersion")
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
