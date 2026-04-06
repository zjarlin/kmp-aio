plugins {
  id("site.addzero.buildlogic.kmp.cmp-lib-cupertino")
}
val libs = versionCatalogs.named("libs")

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(project(":lib:compose:scaffold-spi"))
      implementation(project(":lib:compose:app-sidebar"))
      implementation(project(":lib:compose:app-sidebar-cupertino-adapter"))
      implementation(libs.findLibrary("site-addzero-compose-native-component-searchbar").get())
      implementation(libs.findLibrary("site-addzero-compose-native-component-tree").get())
      implementation(compose.material3)
    }
  }
}
