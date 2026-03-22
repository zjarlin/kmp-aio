plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apps:kcloud:plugins:ssh:client"))
        }
        jvmMain.dependencies {
            api(project(":apps:kcloud:plugins:ssh:server"))
        }
    }
}
