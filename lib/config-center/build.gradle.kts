plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
    id("site.addzero.buildlogic.kmp.kmp-sqldelight")
}

val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
    }
}

sqldelight {
    databases {
        create("ConfigCenterDatabase") {
            packageName.set("site.addzero.configcenter.db")
            dialect(libs.findLibrary("app-cash-sqldelight-dialect-sqlite-3-38").get())
        }
    }
}
