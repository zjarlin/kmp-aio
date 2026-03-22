import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
}

val libs = versionCatalogs.named("libs")

kotlin {
    jvmToolchain(17)
    jvm {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":apps:kcloud:plugins:ai:spi"))
        }
        jvmMain.dependencies {
            implementation(libs.findLibrary("ai-koog-koog-agents").get())
        }
    }
}
