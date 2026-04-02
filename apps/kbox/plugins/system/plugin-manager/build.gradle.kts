plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.ksp.ksp-jvm-cache-preparation")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

val libs = versionCatalogs.named("libs")
val sharedSourceDir = rootProject.file("apps/kbox/shared/src/commonMain/kotlin").absolutePath
val routeOwnerModuleDir = rootProject.file("apps/kbox/composeApp/src/jvmMain/kotlin").absolutePath

ksp {
    arg("sharedSourceDir", sharedSourceDir)
    arg("routeGenPkg", "site.addzero.generated")
    arg("routeOwnerModule", routeOwnerModuleDir)
    arg("routeModuleKey", project.path)
}

dependencies {
    add("kspCommonMainMetadata", libs.findLibrary("site-addzero-route-processor").get())
    add("kspJvm", libs.findLibrary("site-addzero-route-processor").get())
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("site-addzero-route-core").get())
        }
        jvmMain.dependencies {
            implementation(project(":lib:kbox-plugin-api"))
        }
    }
}
