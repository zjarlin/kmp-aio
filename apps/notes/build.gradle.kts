plugins {
    id("site.addzero.buildlogic.kmp.cmp-aio")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:glass-components"))
        }
        jvmMain.dependencies {
            implementation(project(":apps:notes:server"))
        }
    }
}
