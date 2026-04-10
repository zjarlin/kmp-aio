plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

val generatedApiSourceDir = layout.buildDirectory.dir("generated/source/controller2api/commonMain/kotlin")
val generatedKspSourceDir = layout.buildDirectory.dir("generated/ksp/commonMain/kotlin")
val generateCodegenContextApiTaskPath = ":apps:kcloud:plugins:codegen-context:server:kspKotlinJvm"

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir(generatedApiSourceDir)
            kotlin.srcDir(generatedKspSourceDir)
            dependencies {
                implementation(project(":apps:kcloud:shared"))
                implementation(project(":apps:kcloud:plugins:codegen-context:shared"))
            }
        }
    }
}

tasks.matching { task ->
    task.name in setOf(
        "kspKotlinJvm",
        "compileCommonMainKotlinMetadata",
        "compileKotlinJvm",
    )
}.configureEach {
    dependsOn(generateCodegenContextApiTaskPath)
}
