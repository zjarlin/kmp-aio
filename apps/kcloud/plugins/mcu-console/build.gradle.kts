plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
    id("site.addzero.buildlogic.kmp.kmp-json")
}
val generatedApiSourceDir = layout.projectDirectory.dir("generated/commonMain/kotlin")

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir(generatedApiSourceDir)
        }
    }
}
