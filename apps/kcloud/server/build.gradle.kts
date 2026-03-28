plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-server")
}


kotlin {
    sourceSets {
        jvmMain.dependencies {
            // Starter 模块（引入即生效）
            implementation(project(":lib:ktor:starter:starter-spi"))
            implementation(project(":lib:ktor:starter:starter-koin"))
            implementation(project(":lib:ktor:starter:starter-serialization"))
            implementation(project(":lib:ktor:starter:starter-statuspages"))
            implementation(project(":lib:ktor:starter:starter-banner"))
            implementation(project(":lib:ktor:starter:starter-openapi"))
            implementation(project(":lib:ktor:starter:starter-flyway"))
            implementation(project(":apps:kcloud:plugins:mcu-console"))
            implementation(project(":apps:kcloud:plugins:vibepocket"))
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
