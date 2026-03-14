plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apps:kcloud:plugins:dotfiles-plugin"))
        }
        jvmMain.dependencies {
            api(project(":apps:kcloud:plugins:dotfiles-server-plugin"))
        }
    }
}
