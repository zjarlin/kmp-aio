plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apps:kcloud:plugins:notes-plugin"))
        }
        jvmMain.dependencies {
            implementation(project(":apps:kcloud:plugins:plugin-api"))
            api(project(":apps:kcloud:plugins:notes-server-plugin"))
            api(project(":apps:notes:server"))
        }
    }
}
