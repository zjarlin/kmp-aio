plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-server")
    id("site.addzero.buildlogic.kmp.cmp-kcloud-aio")
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            // 第一阶段只保留 MCU 控制台链路，系统插件稍后再恢复。
            implementation(project(":apps:kcloud:shared"))
            implementation(project(":lib:config-center"))
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
