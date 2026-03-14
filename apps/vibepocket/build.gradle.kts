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

val javaFxVersion = "19"
val javaFxClassifier = run {
    val osName = System.getProperty("os.name").lowercase()
    val osArch = System.getProperty("os.arch").lowercase()
    when {
        osName.contains("mac") && (osArch.contains("aarch64") || osArch.contains("arm64")) -> "mac-aarch64"
        osName.contains("mac") -> "mac"
        osName.contains("win") -> "win"
        osArch.contains("aarch64") || osArch.contains("arm64") -> "linux-aarch64"
        else -> "linux"
    }
}

dependencies {
    kspCommonMainMetadata(libs.site.addzero.ioc.processor)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":apps:kcloud:plugins:plugin-api"))
            implementation(libs.site.addzero.ioc.core)
            implementation(project(":lib:media-playlist-player"))
            implementation(project(":lib:vibepocket-ui"))
            implementation(project(":lib:glass-components"))
            implementation(project(":lib:api-music-spi"))
            implementation(project(":lib:api-suno"))
            implementation(libs.io.github.khubaibkhan4.mediaplayer.kmp)
        }
        jvmMain.dependencies {
            implementation(project(":apps:vibepocket:server"))
            implementation("org.openjfx:javafx-base:$javaFxVersion:$javaFxClassifier")
            implementation("org.openjfx:javafx-graphics:$javaFxVersion:$javaFxClassifier")
            implementation("org.openjfx:javafx-media:$javaFxVersion:$javaFxClassifier")
        }
    }
}
