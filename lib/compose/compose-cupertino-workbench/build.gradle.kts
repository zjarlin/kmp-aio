plugins {
  id("site.addzero.buildlogic.kmp.cmp-lib-cupertino")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(project(":lib:compose:scaffold-spi"))
      implementation(project(":lib:compose:app-sidebar"))
      implementation(project(":lib:compose:app-sidebar-cupertino-adapter"))
      implementation("site.addzero:compose-native-component-searchbar:2025.09.30")
      implementation("site.addzero:compose-native-component-tree:2025.09.30")
      implementation(compose.material3)
    }
  }
}
