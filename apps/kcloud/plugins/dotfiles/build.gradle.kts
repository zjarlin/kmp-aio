plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apps:kcloud:plugins:dotfiles:client"))
        }
        jvmMain.dependencies {
            api(project(":apps:kcloud:plugins:dotfiles:server"))
        }
    }
}
