plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:kcloud-core"))
            implementation(libs.org.jetbrains.kotlinx.kotlinx.datetime)
        }
        jvmMain.dependencies {
            implementation(project(":apps:kcloud:features:feature-api"))
            implementation("com.github.kwhat:jnativehook:2.2.2")
        }
    }
}
