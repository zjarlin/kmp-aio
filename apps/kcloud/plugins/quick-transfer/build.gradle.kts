plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apps:kcloud:plugins:quick-transfer:client"))
        }
        jvmMain.dependencies {
            api(project(":apps:kcloud:plugins:quick-transfer:server"))
        }
    }
}
