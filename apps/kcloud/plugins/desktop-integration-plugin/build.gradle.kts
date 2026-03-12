plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":apps:kcloud:plugins:plugin-api"))
            implementation(project(":lib:kcloud-core"))
            implementation("com.github.kwhat:jnativehook:2.2.2")
        }
    }
}
