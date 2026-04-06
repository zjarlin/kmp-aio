plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-json")
    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
    id("site.addzero.buildlogic.kmp.kmp-datetime")
}

val generatedApiSourceDir = layout.projectDirectory.dir("generated/commonMain/kotlin")
val addzeroLibJvmVersion: String by project

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir(generatedApiSourceDir)
            dependencies {
                api("site.addzero:modbus-runtime:$addzeroLibJvmVersion")
            }
        }
    }
}
