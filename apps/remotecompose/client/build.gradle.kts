plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apps:remotecompose:shared"))
            implementation(libs.site.addzero.network.starter)
            implementation(libs.org.jetbrains.kotlinx.kotlinx.serialization.json)
            implementation(libs.io.ktor.ktor.client.content.negotiation)
            implementation(libs.io.ktor.ktor.serialization.kotlinx.json)
        }
    }
}
