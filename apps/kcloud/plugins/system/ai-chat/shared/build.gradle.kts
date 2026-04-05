plugins {
    id("site.addzero.buildlogic.kmp.kmp-config-center")
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-wasm")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
    id("site.addzero.buildlogic.kmp.kmp-json")
    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
}

val generatedApiSourceDir = layout.projectDirectory.dir("generated/commonMain/kotlin")

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir(generatedApiSourceDir)
            dependencies {
                implementation(project(":lib:tool-kmp:network-starter"))
            }
        }
    }
}
