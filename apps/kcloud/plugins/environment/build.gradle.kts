plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apps:kcloud:plugins:environment-plugin"))
        }
        jvmMain.dependencies {
            api(project(":apps:kcloud:plugins:environment-server-plugin"))
        }
    }
}
