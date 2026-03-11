/**
 * KCloud - 类 Nextcloud 的跨平台同步客户端
 *
 * 支持 WebDAV/S3/SSH 多种存储后端，端到端加密
 */
plugins {
    id("site.addzero.buildlogic.kmp.cmp-aio")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

ksp {
    arg("springKtor.generatedPackage", "com.kcloud.server.generated.springktor")
}

kotlin {
    dependencies {
//        implementation(project(":lib:glass-components"))
        implementation(project(":lib:shadcn-ui-kmp"))
        implementation(project(":lib:kcloud-core"))
        implementation(project(":lib:plugin-ui"))
//        implementation(project(":lib:api-suno"))
//        implementation("site.addzero:api-netease:2026.02.17")
//        implementation(libs.io.github.khubaibkhan4.mediaplayer.kmp)
    }

    sourceSets {
        jvmMain.dependencies {
            // 依赖 kcloud-core 传递下来的依赖
            // (SQLite, HikariCP, SSHJ, JmDNS, kotlinx-datetime, kotlin-logging)

            // Spring2Ktor - 使用 Spring 风格编写 Ktor 路由 (仅 JVM)
            implementation("site.addzero:spring2ktor-server-core:2026.03.10")
            compileOnly("org.springframework:spring-web:5.3.21")

            // Ktor Server (内嵌本地服务器)
            implementation("io.ktor:ktor-server-cio-jvm:2.3.12")
            implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.12")
            implementation("io.ktor:ktor-server-websockets-jvm:2.3.12")
            implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.12")
            implementation("io.ktor:ktor-server-cors-jvm:2.3.12")

            // AWS SDK for Kotlin S3
            implementation("aws.sdk.kotlin:s3:1.0.0")

            // JNativeHook - 全局快捷键
            implementation("com.github.kwhat:jnativehook:2.2.2")
        }
    }
}

// KSP 处理器依赖 - 必须在 kotlin 块外声明
dependencies {
    ksp("site.addzero:spring2ktor-server-processor:2026.03.10")
}
