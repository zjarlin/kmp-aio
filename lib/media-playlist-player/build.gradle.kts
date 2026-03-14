plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-coil")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.io.github.khubaibkhan4.mediaplayer.kmp)
        }
    }
}
