plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apps:kcloud:plugins:notes:client"))
        }
        jvmMain.dependencies {
            api(project(":apps:kcloud:plugins:notes:server"))
            api(project(":apps:notes:server"))
        }
    }
}
