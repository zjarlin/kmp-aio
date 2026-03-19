plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apps:kcloud:features:transfer-history:client"))
        }
        jvmMain.dependencies {
            api(project(":apps:kcloud:features:transfer-history:server"))
        }
    }
}
