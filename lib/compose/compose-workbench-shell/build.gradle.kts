plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:compose:compose-workbench-design"))
            implementation("site.addzero:compose-native-component-searchbar:2025.09.30")
            implementation("site.addzero:compose-native-component-tree:2025.09.30")
        }
    }
}
