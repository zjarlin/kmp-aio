plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apps:kcloud:plugins:transfer-history:transfer-history-plugin"))
        }
        jvmMain.dependencies {
            api(project(":apps:kcloud:plugins:transfer-history:transfer-history-server-plugin"))
        }
    }
}
