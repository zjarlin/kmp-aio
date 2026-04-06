plugins {
  id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(project(":lib:compose:compose-native-component-toast"))
      implementation("site.addzero:network-starter:2026.10330.12238")
      implementation("site.addzero:tool-koin:2025.12.30")
    }
  }
}
