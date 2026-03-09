plugins {
    id("site.addzero.buildlogic.kmp.cmp-aio")
}

val markdownRendererVersion = "0.29.0"

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.mikepenz:multiplatform-markdown-renderer-m3:$markdownRendererVersion")
            implementation("com.mikepenz:multiplatform-markdown-renderer-code:$markdownRendererVersion")
        }
        jvmMain.dependencies {
            implementation(project(":apps:notes:server"))
        }
    }
}
