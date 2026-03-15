plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":apps:kcloud:plugins:ai:ai-spi"))
            implementation(project(":apps:kcloud:plugins:settings"))
            implementation(project(":lib:kcloud-core"))
        }
    }
}
