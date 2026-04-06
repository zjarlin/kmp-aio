plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":lib:compose:app-sidebar"))
            api("site.addzero:shadcn-compose-component:2025.09.30")
        }
    }
}
