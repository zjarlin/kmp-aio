plugins {
    id("site.addzero.buildlogic.kmp.cmp-aio")
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":apps:notes:server"))
        }
    }
}
