plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apps:kcloud:features:compose:client"))
        }
        jvmMain.dependencies {
            api(project(":apps:kcloud:features:compose:server"))
        }
    }
}
