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


dependencies {
    kspCommonMainMetadata(libs.site.addzero.ioc.processor)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.site.addzero.ioc.core)
            implementation(project(":lib:glass-components"))
            implementation(project(":lib:shadcn-ui-kmp"))
            implementation(project(":lib:api-suno"))
            implementation("site.addzero:api-netease:2026.02.17")
            implementation(libs.io.github.khubaibkhan4.mediaplayer.kmp)
        }
        jvmMain.dependencies {
            implementation(project(":server"))
        }
    }
}

