plugins {
    id("site.addzero.buildlogic.kmp.cmp-kcloud-aio")
}
val libs = versionCatalogs.named("libs")


dependencies {
//    kspCommonMainMetadata(libs.findLibrary("site-addzero-route-processor"))
}
kotlin{
    dependencies{
//        implementation(libs.findLibrary("site-addzero-route-core"))

    }
}
