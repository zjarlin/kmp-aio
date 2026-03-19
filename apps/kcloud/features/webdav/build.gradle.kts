plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apps:kcloud:features:webdav:client"))
        }
        jvmMain.dependencies {
            api(project(":apps:kcloud:features:webdav:server"))
        }
    }
}
