plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":apps:kcloud:plugins:plugin-api"))
            implementation(project(":apps:kcloud:plugins:quick-transfer-plugin"))
            implementation(project(":lib:kcloud-core"))
        }
    }
}
