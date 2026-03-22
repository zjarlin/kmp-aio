plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apps:kcloud:plugins:file:client"))
        }
        jvmMain.dependencies {
            api(project(":apps:kcloud:plugins:file:server"))
        }
    }
}
