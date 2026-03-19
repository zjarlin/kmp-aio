plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apps:kcloud:features:package-organizer:client"))
        }
        jvmMain.dependencies {
            api(project(":apps:kcloud:features:package-organizer:server"))
        }
    }
}
