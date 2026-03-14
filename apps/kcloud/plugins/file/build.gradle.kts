plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apps:kcloud:plugins:file-plugin"))
        }
        jvmMain.dependencies {
            api(project(":apps:kcloud:plugins:file-server-plugin"))
        }
    }
}
