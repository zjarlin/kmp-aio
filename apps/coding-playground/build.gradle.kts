plugins {
    id("site.addzero.buildlogic.kmp.cmp-aio")
}
val libs = versionCatalogs.named("libs")
val ktorVersion = libs.findVersion("ktor").get().requiredVersion
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":apps:coding-playground:shared"))
        }
        jvmMain.dependencies {
            implementation(project(":lib:ktor:plugin:ktor-jimmer-plugin"))
            implementation(project(":apps:coding-playground:server"))
            implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
            implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
            implementation("io.ktor:ktor-server-cors-jvm:$ktorVersion")
        }
    }
}
