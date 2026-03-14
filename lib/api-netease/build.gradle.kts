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
    sourceSets {
        commonMain.dependencies {
            implementation(libs.site.addzero.network.starter)
            api(projects.lib.apiMusicSpi)
        }
//        commonMain.dependencies {
//            implementation("io.github.ltttttttttttt:LazyPeopleHttp-lib:+")
//        }
//        commonTest.dependencies {
//            implementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.test)
//        }
    }
}
