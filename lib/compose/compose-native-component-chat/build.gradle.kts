plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}
val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.findLibrary("site-addzero-compose-native-component-button").get())
                implementation(project(":lib:compose:compose-native-component-text"))
            }
        }
    }
}
