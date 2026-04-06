plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-datetime")
    id("site.addzero.kcp.spread-pack") apply false
}

extra["site.addzero.kcp.spread-pack.annotations-added"] = true
apply(plugin = "site.addzero.kcp.spread-pack")

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("site.addzero:kcp-spread-pack-annotations:2026.04.04")
                implementation("site.addzero:tool-enum:2026.02.06")
                implementation(project(":lib:tool-kmp:tool-regex"))
                implementation("site.addzero:tool-str:2026.02.28")
                implementation("site.addzero:compose-native-component-button:2025.09.30")
                implementation("site.addzero:compose-native-component-tree:2025.09.30")
            }
        }
    }
}
