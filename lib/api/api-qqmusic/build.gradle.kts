plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")

}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("site.addzero:network-starter:2025.09.30")
//            api(projects.lib.apiMusicSpi)
        }
        commonTest.dependencies {
            implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-coroutines-test").get())
        }

    }
}
