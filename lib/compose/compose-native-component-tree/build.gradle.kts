plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":lib:compose:compose-apple-corner"))
                implementation("site.addzero:compose-native-component-button:2025.09.30")
                implementation("site.addzero:compose-native-component-searchbar:2025.09.30")
                implementation("site.addzero:tool-tree:2026.10330.12238")
            }
        }
    }
}
