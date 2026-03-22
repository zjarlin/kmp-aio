plugins {
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":apps:kcloud:plugins:ai:spi"))
        }
        jvmMain.dependencies {
            implementation(project(":apps:kcloud:plugins:feature-api"))
        }
    }
}
