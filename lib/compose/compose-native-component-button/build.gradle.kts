plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("site.addzero:compose-native-component-high-level:2025.09.30")
            }
        }
    }
}
