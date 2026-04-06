plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-server")
    id("site.addzero.buildlogic.kmp.kmp-config-center")
    id("site.addzero.buildlogic.kmp.cmp-kcloud-aio")
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":apps:kcloud:shared"))
//            implementation(project(":apps:kcloud:plugins:system:ai-chat:server"))
            implementation(project(":lib:config-center"))
            implementation(project(":lib:ktor:starter:starter-spi"))
            implementation(project(":lib:ktor:starter:starter-koin"))
            implementation(project(":lib:ktor:starter:starter-serialization"))
            implementation(project(":lib:ktor:starter:starter-statuspages"))
            implementation(project(":lib:ktor:starter:starter-banner"))
            implementation(project(":lib:ktor:starter:starter-openapi"))
            implementation(project(":lib:ktor:starter:starter-flyway"))
            implementation(project(":lib:ktor:plugin:ktor-jimmer-plugin"))
            implementation(project(":lib:ktor:plugin:ktor-s3-plugin"))
        }
        jvmTest.dependencies {
            implementation(project(":apps:kcloud:plugins:mcu-console"))
        }
    }
}
val serverMainClass = "site.addzero.kcloud.server.ApplicationKt"

kotlin.jvm().mainRun {
    mainClass.set(serverMainClass)
}

tasks.named<JavaExec>("runJvm") {
    mainClass.set(serverMainClass)
}
