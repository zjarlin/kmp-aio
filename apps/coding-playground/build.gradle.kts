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
// 运行时依赖当前会解析到已发布的 Compose 组件，其中包含 Java 24 字节码，桌面启动需用更高版本 JDK。
val desktopRuntimeJavaLauncher = javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(24))
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":apps:coding-playground:shared"))
            implementation(project(":lib:api:api-netease"))
            implementation(libs.findLibrary("site-addzero-compose-native-component-searchbar").get())
            implementation(libs.findLibrary("site-addzero-compose-native-component-tree").get())
        }
        jvmMain.dependencies {
            implementation(project(":lib:ktor:plugin:ktor-jimmer-plugin"))
            implementation(project(":lib:coding-playground-demo-alpha"))
            implementation(project(":lib:coding-playground-demo-beta"))
            implementation(project(":lib:coding-playground-demo-gamma"))
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

tasks.named<JavaExec>("runJvm") {
    mainClass.set(desktopMainClass)
    javaLauncher.set(desktopRuntimeJavaLauncher)
}

tasks.withType<JavaExec>().configureEach {
    if (name == "jvmRun" || name == "runJvm") {
        javaLauncher.set(desktopRuntimeJavaLauncher)
    }
}

compose.desktop {
    application {
        mainClass = desktopMainClass
    }
}
