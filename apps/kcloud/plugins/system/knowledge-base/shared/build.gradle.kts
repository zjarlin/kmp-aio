plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-json")
    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
}

val libs = versionCatalogs.named("libs")
val generatedApiSourceDir = layout.projectDirectory.dir("generated/commonMain/kotlin")

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir(generatedApiSourceDir)
            dependencies {
                implementation(libs.findLibrary("site-addzero-network-starter").get())
            }
        }
    }
}
