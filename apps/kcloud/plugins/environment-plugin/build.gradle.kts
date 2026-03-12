plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":apps:kcloud:plugins:plugin-api"))
            implementation(project(":apps:kcloud:plugins:ssh-plugin"))
            implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-serialization-json").get())
        }
    }
}
