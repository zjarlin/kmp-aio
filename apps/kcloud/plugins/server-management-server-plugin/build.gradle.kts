plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":apps:kcloud:plugins:plugin-api"))
            implementation(project(":apps:kcloud:plugins:server-management-plugin"))
            implementation(project(":lib:kcloud-core"))
            implementation("aws.sdk.kotlin:s3:1.0.0")
        }
    }
}
