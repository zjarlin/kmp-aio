plugins {
    id("site.addzero.buildlogic.kmp.cmp-app")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-konfig")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
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
            implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
            implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
            implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
            implementation("io.ktor:ktor-server-cors-jvm:$ktorVersion")
        }
        jvmTest.dependencies {
            implementation(libs.findLibrary("org-babyfish-jimmer-jimmer-sql-kotlin").get())
            implementation(libs.findLibrary("org-xerial-sqlite-jdbc-v3").get())
        }
    }
}
