/**
 * VibePocket 应用模块 - KMP Compose Multiplatform 桌面应用
 *
 * 复制此模块创建新应用:
 * 1. 复制 apps/vibepocket 到 apps/{your-app-name}
 * 2. 修改 namespace 和 artifact
 * 3. 更新依赖
 */
plugins {
    id("site.addzero.buildlogic.kmp.cmp-app")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-filekit")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
}

// 应用配置
val appName = "vibepocket"
val appNamespace = "site.addzero.vibepocket"

dependencies {
    kspCommonMainMetadata(libs.site.addzero.ioc.processor)
}

kotlin {
    dependencies {
        implementation(libs.site.addzero.ioc.core)
        implementation(libs.site.addzero.network.starter)
        implementation(project(":lib:glass-components"))
        implementation(project(":lib:shadcn-ui-kmp"))
        implementation(project(":lib:api-suno"))
        implementation("site.addzero:api-netease:2026.02.17")
        implementation(libs.io.github.khubaibkhan4.mediaplayer.kmp)
    }
    sourceSets {
        jvmMain.dependencies {
            implementation(libs.io.ktor.ktor.server.netty.jvm)
            // 桌面端内嵌 Ktor 后端
            implementation(project(":server"))
        }
    }
}

// JVM 桌面打包配置
compose.desktop {
    application {
        mainClass = "$appNamespace.MainKt"
        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
            )
            packageName = appName
            packageVersion = "1.0.0"

            // 使用应用名作为输出文件名
            outputBaseDir.set(project.layout.buildDirectory.dir("compose-binaries"))

            // macOS 配置
            macOS {
                bundleID = "$appNamespace.desktop"
                iconFile.set(project.file("src/jvmMain/resources/icon.icns"))
            }

            // Windows 配置
            windows {
                iconFile.set(project.file("src/jvmMain/resources/icon.ico"))
                menuGroup = appName
//                upgradeUuid = "${UUID.randomUUID()}"
            }

            // Linux 配置
            linux {
                iconFile.set(project.file("src/jvmMain/resources/icon.png"))
                debMaintainer = "admin@addzero.site"
                appRelease = "1"
                appCategory = "AudioVideo"
            }
        }
    }
}
