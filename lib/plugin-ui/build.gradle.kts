plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            // Compose 依赖由 cmp-lib 插件提供
        }
    }
}
