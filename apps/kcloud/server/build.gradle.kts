import org.gradle.api.tasks.JavaExec

plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-server")
}


val libs = versionCatalogs.named("libs")
val serverMainClass = "site.addzero.kcloud.server.ApplicationKt"

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":apps:kcloud:plugins:codegen-context:server"))
            implementation(project(":apps:kcloud:plugins:host-config:server"))
            implementation(project(":apps:kcloud:plugins:mcu-console:server"))
            api(project(":lib:ktor:starter:starter-spi"))
            api(project(":lib:ktor:starter:starter-koin"))
            api(project(":lib:ktor:starter:starter-serialization"))
            api(project(":lib:ktor:starter:starter-statuspages"))
            api(project(":lib:ktor:starter:starter-banner"))
            api(project(":lib:ktor:starter:starter-openapi"))
            api(project(":lib:ktor:starter:starter-flyway"))
            api(project(":lib:ktor:plugin:ktor-jimmer-plugin"))
            api(project(":lib:ktor:plugin:ktor-s3-plugin"))
            implementation(libs.findLibrary("org-xerial-sqlite-jdbc-v3").get())
        }
    }
}

tasks.named<JavaExec>("runJvm") {
    mainClass.set(serverMainClass)
}
