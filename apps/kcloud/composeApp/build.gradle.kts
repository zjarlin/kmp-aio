@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

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


kotlin {
    dependencies {
//        implementation(project(":lib:compose:media-playlist-player"))
//        implementation(project(":lib:compose:app-sidebar"))
//        implementation(project(":lib:compose:workbench-shell"))
//        implementation(projects.lib.compose.liquidGlass)
//        implementation(project(":lib:api:api-music-spi"))
//        implementation(project(":lib:api:api-suno"))
    }

//    sourceSets {
//        jvmMain.dependencies {
//            implementation(project(":apps:vibepocket:server"))
//        }
//    }
}



