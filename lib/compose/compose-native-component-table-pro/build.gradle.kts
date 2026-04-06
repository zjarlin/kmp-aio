
plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
//    id("kmp-koin")
//    id("kmp-koin")
//    id("kmp-ksp-plugin")
}
//dependencies {
//    kspCommonMainMetadata(project(":lib:compose:compose-props-processor"))
//}
kotlin {
    sourceSets {
        commonMain.dependencies {
//            implementation("site.addzero:tool-json:2026.02.04")
            implementation("site.addzero:compose-props-annotations:2025.09.30")
//            implementation(projects.lib.compose.addzerosearch)
            api(project(":lib:compose:compose-crud-spi"))
            api("site.addzero:compose-native-component-table:2025.09.30")
            implementation("site.addzero:compose-native-component-button:2025.09.30")
            implementation("site.addzero:compose-native-component-searchbar:2025.09.30")
            implementation("site.addzero:compose-native-component-assist:2025.09.30")
            implementation("site.addzero:compose-native-component-select:2025.09.30")
            implementation("site.addzero:compose-native-component-high-level:2025.09.30")
            implementation("site.addzero:compose-native-component-form:2025.09.30")
            implementation("site.addzero:compose-native-component-tree:2025.09.30")
            implementation("site.addzero:compose-native-component-card:2025.09.30")
//            implementation("site.addzero:tool-str:2026.02.28")
//            implementation(projects.lib.toolKmp.tool)
        }
    }
}
