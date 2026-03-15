plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apps:kcloud:plugins:notes:notes-plugin"))
        }
        jvmMain.dependencies {
            api(project(":apps:kcloud:plugins:notes:notes-server-plugin"))
            api(project(":apps:notes:server"))
        }
    }
}
