plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-json")
}

val generatedKspSourceDir = layout.buildDirectory.dir("generated/ksp/commonMain/kotlin")

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir(generatedKspSourceDir)
            dependencies {
            }
        }
    }
}
