plugins {
    kotlin("multiplatform")
    id("io.insert-koin.compiler.plugin")
}

kotlin {
    jvm()
    jvmToolchain(17)

    sourceSets {
        commonMain.dependencies {
            implementation(project(":feature-alpha"))

            implementation(project.dependencies.platform("io.insert-koin:koin-bom:4.2.0-RC1"))
            implementation("io.insert-koin:koin-annotations")
            implementation("io.insert-koin:koin-core")
        }
    }
}

koinCompiler {
    userLogs = true
    debugLogs = true
    dslSafetyChecks = true
}
