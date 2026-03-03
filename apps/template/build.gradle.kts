/**
 * APP_TEMPLATE_NAME 应用模块 - KMP Compose Multiplatform 桌面应用
 *
 * 使用说明:
 * 1. 复制此目录到 apps/{your-app-name}
 * 2. 替换 APP_TEMPLATE_NAME 为你的应用名
 * 3. 修改 appName 和 appNamespace
 * 4. 添加业务依赖
 * 5. 在 src 目录下编写代码
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

// ==================== 应用配置（修改这里）====================
val appName = project.name
val appNamespace = "site.addzero.${project.name}"
val appVersion = project.version.toString()
// ===========================================================

dependencies {
    kspCommonMainMetadata(libs.site.addzero.ioc.processor)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // 基础依赖
            implementation(libs.site.addzero.ioc.core)
            implementation(libs.site.addzero.network.starter)

            // UI 组件库
            implementation(project(":lib:glass-components"))
            implementation(project(":lib:shadcn-ui-kmp"))

            // 业务 API 库（按需添加）
            // implementation(project(":lib:api-suno"))
            // implementation("site.addzero:api-netease:2026.02.17")
        }
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
            packageVersion = appVersion

            // 输出目录
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
                // 生成一个固定的 UUID 供模板使用
                upgradeUuid = "00000000-0000-0000-0000-000000000001"
            }

            // Linux 配置
            linux {
                iconFile.set(project.file("src/jvmMain/resources/icon.png"))
                debMaintainer = "admin@addzero.site"
                appRelease = "1"
                appCategory = "Utility"
            }
        }
    }
}
