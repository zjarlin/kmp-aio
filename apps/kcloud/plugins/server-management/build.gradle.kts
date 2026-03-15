plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apps:kcloud:plugins:server-management:server-management-plugin"))
        }
        jvmMain.dependencies {
            api(project(":apps:kcloud:plugins:server-management:server-management-server-plugin"))
        }
    }
}
