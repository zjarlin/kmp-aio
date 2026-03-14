plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    kotlin("plugin.serialization")
}

val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":lib:kcloud-core"))
            implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-serialization-json").get())
        }
    }
}
