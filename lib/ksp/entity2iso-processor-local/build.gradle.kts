plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
}

val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("site-addzero-lsi-core").get())
            implementation(libs.findLibrary("jimmer-entity-spi").get())
        }
    }
}
