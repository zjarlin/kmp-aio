plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apps:kcloud:plugins:ai:client"))
            api(project(":apps:kcloud:plugins:ai:spi"))
        }
        jvmMain.dependencies {
            api(project(":apps:kcloud:plugins:ai:server"))
            api(project(":apps:kcloud:plugins:ai:ollama-provider"))
        }
    }
}
