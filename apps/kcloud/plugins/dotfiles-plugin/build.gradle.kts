plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":apps:kcloud:plugins:plugin-api"))
            implementation(libs.org.jetbrains.kotlinx.kotlinx.serialization.json)
        }
    }
}
