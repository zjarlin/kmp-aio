plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
}

val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":apps:kcloud:plugins:feature-api"))
            implementation(project(":apps:kcloud:plugins:ssh:client"))
            implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-serialization-json").get())
        }
    }
}
