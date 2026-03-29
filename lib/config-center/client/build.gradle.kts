plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-json")
    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":lib:config-center:spec"))
        }
    }
}
