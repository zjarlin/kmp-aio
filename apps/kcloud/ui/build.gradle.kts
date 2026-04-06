@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Zip
import org.gradle.declarative.dsl.schema.FqName.Empty.packageName
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("site.addzero.buildlogic.kmp.cmp-app-cupertino")
    id("site.addzero.buildlogic.kmp.cmp-wasm")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
    //addzero组件库
    id("site.addzero.buildlogic.conventions.addzero-component")
}

val libs = versionCatalogs.named("libs")
val addzeroLibJvmVersion: String by project
val desktopMainClass = "site.addzero.kcloud.bootstrap.MainKt"

kotlin {
    dependencies {
        implementation(project(":apps:kcloud:plugins:mcu-console:ui"))

        implementation("site.addzero:compose-cupertino-workbench:$addzeroLibJvmVersion")
        implementation("site.addzero:scaffold-spi:$addzeroLibJvmVersion")
        implementation("site.addzero:compose-icon-map:2026.10329.10127")
        implementation(project(":apps:kcloud:shared"))
        implementation(libs.findLibrary("site-addzero-network-starter").get())
        implementation("site.addzero:compose-native-component-chat:$addzeroLibJvmVersion")
        implementation(libs.findLibrary("site-addzero-compose-native-component-searchbar").get())
        implementation(libs.findLibrary("site-addzero-compose-native-component-tree").get())
    }
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":apps:kcloud:plugins:mcu-console:ui"))
            implementation(project(":lib:ktor:starter:starter-spi"))
            implementation("site.addzero:compose-workbench-immersive-desktop:$addzeroLibJvmVersion")

            //后端
            implementation(project(":apps:kcloud:server"))
            implementation(libs.findLibrary("io-ktor-ktor-server-core-jvm").get())
        }
        jvmTest.dependencies {
            implementation(project(":apps:kcloud:server"))
//            implementation(project(":apps:kcloud:plugins:vibepocket:server"))
            implementation(libs.findLibrary("io-ktor-ktor-server-core-jvm").get())
        }
    }
}

kotlin.jvm().mainRun {
    mainClass.set(desktopMainClass)
}
compose.desktop {
    application {
        mainClass = desktopMainClass
        nativeDistributions {
            packageName = "OKMY DICS"
        }
    }
}
