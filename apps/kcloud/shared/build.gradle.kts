plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-json")
}

val libs = versionCatalogs.named("libs")
val routeSnapshotTasks = listOf(
    ":apps:kcloud:plugins:mcu-console:kspCommonMainKotlinMetadata",
    ":apps:kcloud:plugins:system:rbac:kspCommonMainKotlinMetadata",
    ":apps:kcloud:plugins:vibepocket:kspCommonMainKotlinMetadata",
)

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.findLibrary("site-addzero-route-core").get())
        }
    }
}

tasks.matching { it.name in setOf("compileCommonMainKotlinMetadata", "compileKotlinJvm") }
    .configureEach {
        dependsOn(routeSnapshotTasks)
    }
