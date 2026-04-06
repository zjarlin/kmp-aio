plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("site.addzero:shadcn-compose-component:2025.09.30")
        }
    }
}
