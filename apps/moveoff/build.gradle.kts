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
//        implementation(project(":lib:glass-components"))
        implementation(project(":lib:shadcn-ui-kmp"))
        implementation("io.github.oshai:kotlin-logging:7.0.3")
        implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
//        implementation(project(":lib:api-suno"))
//        implementation("site.addzero:api-netease:2026.02.17")
//        implementation(libs.io.github.khubaibkhan4.mediaplayer.kmp)
    }

    sourceSets {
        jvmMain.dependencies {
            // Ktor Server (内嵌本地服务器)
            implementation("io.ktor:ktor-server-cio-jvm:2.3.12")
            implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.12")
            implementation("io.ktor:ktor-server-websockets-jvm:2.3.12")
            implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.12")
            implementation("io.ktor:ktor-server-cors-jvm:2.3.12")

            // SQLite JDBC驱动
            implementation("org.xerial:sqlite-jdbc:3.45.1.0")
            // HikariCP连接池
            implementation("com.zaxxer:HikariCP:5.1.0")

            // AWS SDK for Kotlin S3
            implementation("aws.sdk.kotlin:s3:1.0.0")

            // JNativeHook - 全局快捷键
            implementation("com.github.kwhat:jnativehook:2.2.2")

            // SSHJ - SSH/SFTP 客户端
            implementation("com.hierynomus:sshj:0.38.0")

            // JmDNS - mDNS 服务发现 (P2P)
            implementation("org.jmdns:jmdns:3.5.9")
        }
    }
}
