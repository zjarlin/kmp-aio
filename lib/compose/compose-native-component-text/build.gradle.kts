plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
    id("site.addzero.kcp.spread-pack") apply false
}

extra["site.addzero.kcp.spread-pack.annotations-added"] = true
apply(plugin = "site.addzero.kcp.spread-pack")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("site.addzero:kcp-spread-pack-annotations:2026.04.04")
            implementation(project(":lib:compose:compose-native-component-autocomplet"))
        }
    }
}
