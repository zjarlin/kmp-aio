plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":apps:kcloud:features:ai:spi"))
            implementation(project(":apps:kcloud:features:settings"))
            implementation(project(":lib:kcloud-core"))
        }
    }
}
