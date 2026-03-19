plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apps:kcloud:features:notes:client"))
        }
        jvmMain.dependencies {
            api(project(":apps:kcloud:features:notes:server"))
            api(project(":apps:notes:server")) {
                exclude(group = "org.slf4j", module = "slf4j-simple")
            }
        }
    }
}
