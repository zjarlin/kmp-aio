plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
//    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}
//dependencies {
//    kspCommonMainMetadata("io.github.ltttttttttttt:LazyPeopleHttp:2.2.5")
//}

kotlin {
    dependencies {
        implementation(libs.findLibrary("site-addzero-network-starter").get())
//        api(projects.lib.apiMusicSpi)

    }
}
