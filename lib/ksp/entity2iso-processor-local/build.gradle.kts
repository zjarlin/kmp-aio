plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
}

val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("jimmer-entity-spi").get())
        }
    }
}
