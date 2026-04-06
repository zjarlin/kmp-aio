import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}
kotlin {
    sourceSets {
        commonMain {
            dependencies {

                implementation("site.addzero:compose-native-component-button:2025.09.30")

            }
        }
    }

}


