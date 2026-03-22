plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")

    id("site.addzero.buildlogic.kmp.kmp-json-withtool")

}

kotlin {
    dependencies {
        implementation(libs.findLibrary("site-addzero-network-starter").get())
//        api(projects.lib.apiMusicSpi)
    }
    sourceSets {

    }
}
