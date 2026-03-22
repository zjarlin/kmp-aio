plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.org.jetbrains.kotlinx.kotlinx.serialization.json)
        }
    }
}
