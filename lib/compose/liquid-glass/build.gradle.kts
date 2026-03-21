plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.io.github.fletchmckee.liquid.liquid)
        }
    }
}
