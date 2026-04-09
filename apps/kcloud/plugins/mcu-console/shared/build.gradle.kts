plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-json")
    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
    id("site.addzero.buildlogic.kmp.kmp-datetime")
}
val libs = versionCatalogs.named("libs")

val generatedKspSourceDir = layout.buildDirectory.dir("generated/ksp/commonMain/kotlin")
val generatedContractSourceDir = layout.projectDirectory.dir("generated/commonMain/kotlin")
val addzeroLibJvmVersion: String by project
val generateMcuConsoleContractsTask = ":apps:kcloud:plugins:codegen-context:server:generateMcuConsoleContracts"

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir(generatedKspSourceDir)
            kotlin.srcDir(generatedContractSourceDir)
            dependencies {
                implementation(libs.findLibrary("modbus-runtime").get())
//                implementation(libs.findLibrary("modbus-runtime").get())
//                implementation(libs.findLibrary("modbus-runtime").get())
            }
        }
    }
}

tasks.configureEach {
    if (
        name in setOf(
            "compileCommonMainKotlinMetadata",
            "compileKotlinJvm",
            "jvmSourcesJar",
            "jvmJar",
        )
    ) {
        dependsOn(generateMcuConsoleContractsTask)
    }
}
