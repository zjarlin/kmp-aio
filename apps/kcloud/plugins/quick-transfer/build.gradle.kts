plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apps:kcloud:plugins:quick-transfer:quick-transfer-plugin"))
        }
        jvmMain.dependencies {
            api(project(":apps:kcloud:plugins:quick-transfer:quick-transfer-server-plugin"))
        }
    }
}
