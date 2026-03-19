plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":apps:kcloud:features:feature-api"))
            implementation(project(":apps:kcloud:features:server-management:client"))
            implementation(libs.org.jetbrains.kotlinx.kotlinx.serialization.json)
        }
    }
}
