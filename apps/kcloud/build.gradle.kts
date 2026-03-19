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
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

apply(from = rootProject.file("gradle/spring2ktor-ksp-cache-workaround.gradle.kts"))

val desktopMainClass = "com.kcloud.MainKt"
val libs = versionCatalogs.named("libs")
val ktorVersion = libs.findVersion("ktor").get().requiredVersion

ksp {
    arg("springKtor.generatedPackage", "com.kcloud.app.generated.springktor")
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
            implementation(project(":apps:kcloud:features:ai"))
            implementation(project(":apps:kcloud:features:compose"))
            implementation(project(":apps:kcloud:features:desktop-integration"))
            implementation(project(":apps:kcloud:features:dotfiles"))
            implementation(project(":apps:kcloud:features:environment"))
            implementation(project(":apps:kcloud:features:file"))
            implementation(project(":apps:kcloud:features:notes"))
            implementation(project(":apps:kcloud:features:feature-api"))
            implementation(project(":apps:kcloud:features:package-organizer"))
            implementation(project(":apps:kcloud:features:quick-transfer"))
            implementation(project(":apps:kcloud:features:settings"))
            implementation(project(":apps:kcloud:features:server-management"))
            implementation(project(":apps:kcloud:features:ssh"))
            implementation(project(":apps:kcloud:features:transfer-history"))
            implementation(project(":apps:kcloud:features:webdav"))
        }
        jvmMain.dependencies {
            implementation("io.ktor:ktor-server-cio-jvm:$ktorVersion")
            implementation(libs.findLibrary("io-ktor-ktor-server-content-negotiation").get())
            implementation(libs.findLibrary("io-ktor-ktor-serialization-kotlinx-json").get())
            implementation("io.ktor:ktor-server-cors-jvm:$ktorVersion")
            implementation(projects.lib.starterStatuspages)
            implementation("site.addzero:spring2ktor-server-core:2026.03.13")
            compileOnly("org.springframework:spring-web:5.3.21")
        }
    }
}

dependencies {
    add("kspJvm", "site.addzero:spring2ktor-server-processor:2026.03.13")
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
