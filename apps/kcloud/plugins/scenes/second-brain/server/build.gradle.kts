plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-json")
}

val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":apps:kcloud:plugins:scene-api"))
            implementation(project(":lib:compose:workbench-shell"))
        }
        jvmMain.dependencies {
            implementation(libs.findLibrary("io-ktor-ktor-server-core-jvm").get())
        }
    }
}
