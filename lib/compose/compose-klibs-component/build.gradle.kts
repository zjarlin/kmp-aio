plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}
val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
//            implementation("site.addzero:tool:2025.10.07")
            implementation("site.addzero:compose-model-component:2025.09.30")
//            implementation("com.seanproctor:data-table-material3:0.11.4")
//           implementation("io.github.aleksandar-stefanovic:composematerialdatatable:1.2.1")
            implementation(libs.findLibrary("io-github-vinceglb-filekit-compose").get())
            implementation(libs.findLibrary("io-coil-kt-coil3-coil-compose").get())
            implementation(libs.findLibrary("io-coil-kt-coil3-coil-network-ktor3").get())
        }
    }
}
// build.gradle.kts


//tasks {
//    compileKotlinWasmJs {
//        dependsOn("kspCommonMainKotlinMetadata")
//    }
//
//    // 当所有任务都注册后再配置依赖关系
//    afterEvaluate {
//        tasks.matching { it.name.contains("SourcesJar", true) }.configureEach {
//            dependsOn("kspCommonMainKotlinMetadata")
//        }
//    }
//}
