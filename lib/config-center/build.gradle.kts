plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
    id("site.addzero.buildlogic.kmp.kmp-json")
}

val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(libs.findLibrary("io-ktor-ktor-server-core").get())
            implementation(libs.findLibrary("org-postgresql-postgresql").get())
            implementation(libs.findLibrary("org-xerial-sqlite-jdbc-v3").get())
        }
        jvmTest.dependencies {
            implementation(libs.findLibrary("ktor-server-test-host").get())
            implementation(libs.findLibrary("org-xerial-sqlite-jdbc-v3").get())
        }
    }
}
