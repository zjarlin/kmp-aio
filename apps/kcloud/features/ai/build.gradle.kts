plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apps:kcloud:features:ai:client"))
            api(project(":apps:kcloud:features:ai:spi"))
        }
        jvmMain.dependencies {
            api(project(":apps:kcloud:features:ai:server"))
            api(project(":apps:kcloud:features:ai:ollama-provider"))
        }
    }
}
