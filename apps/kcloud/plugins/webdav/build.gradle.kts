plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apps:kcloud:plugins:webdav:client"))
        }
        jvmMain.dependencies {
            api(project(":apps:kcloud:plugins:webdav:server"))
        }
    }
}
