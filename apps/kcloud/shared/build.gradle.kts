plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-wasm")
    id("site.addzero.buildlogic.kmp.kmp-json")
    id("site.addzero.buildlogic.kmp.cmp-kcloud-aio")
}

kotlin {
    dependencies {
        implementation("site.addzero:route-core:2025.09.30")
    }
}
