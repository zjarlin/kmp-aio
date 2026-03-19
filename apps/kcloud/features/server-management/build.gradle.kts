plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apps:kcloud:features:server-management:client"))
        }
        jvmMain.dependencies {
            api(project(":apps:kcloud:features:server-management:server"))
        }
    }
}
