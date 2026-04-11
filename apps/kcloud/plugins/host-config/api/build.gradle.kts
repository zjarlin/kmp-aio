plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

val generatedApiSourceDir = layout.buildDirectory.dir("generated/source/controller2api/commonMain/kotlin")
val generatedKspSourceDir = layout.projectDirectory.dir("generated/commonMain/kotlin")
val generateHostConfigApiTaskPath = ":apps:kcloud:plugins:host-config:server:kspKotlinJvm"

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir(generatedApiSourceDir)
            kotlin.srcDir(generatedKspSourceDir)
            dependencies {
                implementation(project(":apps:kcloud:plugins:host-config:shared"))
            }
        }
    }
}

tasks.matching { task ->
    task.name in setOf(
        "kspCommonMainKotlinMetadata",
        "kspKotlinJvm",
        "compileCommonMainKotlinMetadata",
        "compileKotlinJvm",
    )
}.configureEach {
    dependsOn(generateHostConfigApiTaskPath)
}
