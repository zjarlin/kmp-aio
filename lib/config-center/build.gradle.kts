plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
}

val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(libs.findLibrary("site-addzero-tool-sql-executor").get())
            implementation(libs.findLibrary("org-postgresql-postgresql").get())
            implementation(libs.findLibrary("org-xerial-sqlite-jdbc-v3").get())
        }
        jvmTest.dependencies {
            implementation(libs.findLibrary("org-xerial-sqlite-jdbc-v3").get())
        }
    }
}
