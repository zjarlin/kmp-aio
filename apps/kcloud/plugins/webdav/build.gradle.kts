plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apps:kcloud:plugins:webdav:webdav-plugin"))
        }
        jvmMain.dependencies {
            api(project(":apps:kcloud:plugins:webdav:webdav-server-plugin"))
        }
    }
}
