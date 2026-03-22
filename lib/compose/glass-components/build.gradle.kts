plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(libs.findLibrary("org-jetbrains-compose-ui-ui-test").get())
        }
    }
}
