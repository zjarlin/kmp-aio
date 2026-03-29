plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-json")
}

val libs = versionCatalogs.named("libs")
val routeSnapshotTasks = listOf(
    // <managed:plugin-market-route-tasks:start>
    ":apps:kcloud:plugins:mcu-console:compileKotlinJvm",
    ":apps:kcloud:plugins:system:config-center:compileKotlinJvm",
    ":apps:kcloud:plugins:system:ai-chat:compileKotlinJvm",
    ":apps:kcloud:plugins:system:knowledge-base:compileKotlinJvm",
    ":apps:kcloud:plugins:system:plugin-market:compileKotlinJvm",
    ":apps:kcloud:plugins:system:rbac:compileKotlinJvm",
    ":apps:kcloud:plugins:vibepocket:compileKotlinJvm",
    // <managed:plugin-market-route-tasks:end>
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
