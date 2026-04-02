plugins {
    id("site.addzero.buildlogic.kmp.kmp-config-center")
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
}
val generatedApiSourceDir = layout.projectDirectory.dir("generated/commonMain/kotlin")

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir(generatedApiSourceDir)
            dependencies {
                api(project(":lib:api:api-music-spi"))
                api(project(":lib:api:api-suno"))
                api(project(":lib:api:api-netease"))
                api(project(":lib:api:api-qqmusic"))
            }
        }
    }
}
