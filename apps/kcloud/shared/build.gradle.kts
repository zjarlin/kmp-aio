plugins {
    id("site.addzero.buildlogic.kmp.kmp-config-center")
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-wasm")
    id("site.addzero.buildlogic.kmp.kmp-json")
    id("site.addzero.buildlogic.kmp.cmp-kcloud-aio")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":lib:ksp:route:route-core"))
        }
    }
}

tasks.matching { task ->
    task.name in setOf("kspCommonMainKotlinMetadata", "kspKotlinJvm")
}.configureEach {
    dependsOn("generateKCloudRouteArtifacts")
}
