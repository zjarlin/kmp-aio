plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-server")
}

val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":apps:kcloud:shared"))
//            implementation(project(":apps:kcloud:plugins:system:ai-chat:server"))
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
//        jvmTest.dependencies {
//            implementation(project(":apps:kcloud:plugins:mcu-console"))
//        }
    }
}
