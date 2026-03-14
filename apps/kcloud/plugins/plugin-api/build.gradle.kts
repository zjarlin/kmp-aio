plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.findLibrary("io-insert-koin-koin-core").get())
            api(libs.findLibrary("io-insert-koin-koin-compose").get())
            api(libs.findLibrary("io-ktor-ktor-server-core").get())
            api(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-serialization-json").get())
        }
        jvmMain.dependencies {
            implementation(project(":lib:kcloud-paths"))
        }
    }
}
