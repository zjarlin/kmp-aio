plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apps:kcloud:plugins:package-organizer:package-organizer-plugin"))
        }
        jvmMain.dependencies {
            api(project(":apps:kcloud:plugins:package-organizer:package-organizer-server-plugin"))
        }
    }
}
