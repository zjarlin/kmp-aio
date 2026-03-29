plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-server")
    id("site.addzero.buildlogic.kmp.cmp-kcloud-aio")
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            // Starter 模块（引入即生效）
            implementation(project(":apps:kcloud:plugins:system:rbac:shared"))
            implementation(project(":apps:kcloud:plugins:system:ai-chat:shared"))
            implementation(project(":apps:kcloud:plugins:system:knowledge-base:shared"))
            implementation(project(":apps:kcloud:plugins:system:plugin-market:shared"))
            implementation(project(":apps:kcloud:plugins:system:config-center:server"))
            implementation(project(":lib:ktor:starter:starter-spi"))
            implementation(project(":lib:ktor:starter:starter-koin"))
            implementation(project(":lib:ktor:starter:starter-serialization"))
            implementation(project(":lib:ktor:starter:starter-statuspages"))
            implementation(project(":lib:ktor:starter:starter-banner"))
            implementation(project(":lib:ktor:starter:starter-openapi"))
            implementation(project(":lib:ktor:starter:starter-flyway"))
            implementation(project(":lib:ktor:plugin:ktor-jimmer-plugin"))
        }
        jvmTest.dependencies {
            implementation(project(":apps:kcloud:plugins:mcu-console"))
        }
    }
}
val serverMainClass = "site.addzero.kcloud.ApplicationKt"

kotlin.jvm().mainRun {
    mainClass.set(serverMainClass)
}

tasks.named<JavaExec>("runJvm") {
    mainClass.set(serverMainClass)
}
