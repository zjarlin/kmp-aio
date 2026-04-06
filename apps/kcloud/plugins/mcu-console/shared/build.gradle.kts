plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-json")
    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
    id("site.addzero.buildlogic.kmp.kmp-datetime")
}
val libs = versionCatalogs.named("libs")

val generatedApiSourceDir = layout.projectDirectory.dir("generated/commonMain/kotlin")
val addzeroLibJvmVersion: String by project

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir(generatedApiSourceDir)
            dependencies {
                implementation(libs.findLibrary("modbus-runtime").get())
//                implementation(libs.findLibrary("modbus-runtime").get())
//                implementation(libs.findLibrary("modbus-runtime").get())
            }
        }
    }
}
