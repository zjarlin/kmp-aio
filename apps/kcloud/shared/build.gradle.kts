plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-wasm")
    id("site.addzero.buildlogic.kmp.kmp-json")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
//    id("site.addzero.buildlogic.kmp.cmp-kcloud-aio")
}
val libs = versionCatalogs.named("libs")

kotlin {
    dependencies {
        implementation(libs.findLibrary("site-addzero-route-core").get())

//        libs.fin
//        site-addzero-route-core
    }
}
