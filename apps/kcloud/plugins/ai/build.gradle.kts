plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apps:kcloud:plugins:ai:ai-plugin"))
            api(project(":apps:kcloud:plugins:ai:ai-spi"))
        }
        jvmMain.dependencies {
            api(project(":apps:kcloud:plugins:ai:ai-server-plugin"))
            api(project(":apps:kcloud:plugins:ai:ollama-provider-plugin"))
        }
    }
}
