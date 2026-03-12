/**
 * KCloud - 类 Nextcloud 的跨平台同步客户端
 *
 * 支持 WebDAV/S3/SSH 多种存储后端，端到端加密
 */
plugins {
    id("site.addzero.buildlogic.kmp.cmp-aio")
}

val desktopMainClass = "com.kcloud.MainKt"
val libs = versionCatalogs.named("libs")
val ktorVersion = libs.findVersion("ktor").get().requiredVersion

kotlin {
    dependencies {
        implementation(project(":apps:kcloud:plugins:plugin-api"))
        implementation(project(":apps:kcloud:plugins:file-plugin"))
        implementation(project(":apps:kcloud:plugins:file-server-plugin"))
        implementation(project(":apps:kcloud:plugins:notes-plugin"))
        implementation(project(":apps:kcloud:plugins:notes-server-plugin"))
        implementation(project(":apps:kcloud:plugins:package-organizer-plugin"))
        implementation(project(":apps:kcloud:plugins:package-organizer-server-plugin"))
        implementation(project(":apps:kcloud:plugins:quick-transfer-plugin"))
        implementation(project(":apps:kcloud:plugins:quick-transfer-server-plugin"))
        implementation(project(":apps:kcloud:plugins:transfer-history-plugin"))
        implementation(project(":apps:kcloud:plugins:transfer-history-server-plugin"))
        implementation(project(":apps:kcloud:plugins:server-management-plugin"))
        implementation(project(":apps:kcloud:plugins:server-management-server-plugin"))
        implementation(project(":apps:kcloud:plugins:ssh-plugin"))
        implementation(project(":apps:kcloud:plugins:ssh-server-plugin"))
        implementation(project(":apps:kcloud:plugins:settings-plugin"))
        implementation(project(":apps:kcloud:plugins:webdav-plugin"))
        implementation(project(":apps:kcloud:plugins:webdav-server-plugin"))
        implementation(project(":apps:kcloud:plugins:dotfiles-plugin"))
        implementation(project(":apps:kcloud:plugins:dotfiles-server-plugin"))
        implementation(project(":apps:kcloud:plugins:environment-plugin"))
        implementation(project(":apps:kcloud:plugins:environment-server-plugin"))
        implementation(project(":apps:kcloud:plugins:desktop-integration-plugin"))
    }

    sourceSets {
        jvmMain.dependencies {
            implementation("io.ktor:ktor-server-cio-jvm:$ktorVersion")
            implementation(libs.findLibrary("io-ktor-ktor-server-content-negotiation").get())
            implementation(libs.findLibrary("io-ktor-ktor-serialization-kotlinx-json").get())
            implementation("io.ktor:ktor-server-cors-jvm:$ktorVersion")
        }
    }
}

kotlin.jvm().mainRun {
    mainClass.set(desktopMainClass)
}

compose.desktop {
    application {
        mainClass = desktopMainClass
    }
}
