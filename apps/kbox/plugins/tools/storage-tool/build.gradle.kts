plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

val libs = versionCatalogs.named("libs")
val addzeroLibJvmVersion: String by project
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
            implementation("site.addzero:scaffold-spi:$addzeroLibJvmVersion")
            implementation(libs.findLibrary("site-addzero-route-core").get())
        }
        jvmMain.dependencies {
            implementation(project(":lib:kbox-core"))
            implementation(project(":lib:kbox-plugin-api"))
        }
    }
}

tasks.matching { task ->
    task.name == "kspKotlinJvm"
}.configureEach {
    doFirst {
        delete(layout.buildDirectory.dir("kspCaches/jvm/jvmMain/symbolLookups"))
        layout.buildDirectory.dir("kspCaches/jvm/jvmMain/symbols").get().asFile.mkdirs()
        layout.buildDirectory.dir("kspCaches/jvm/jvmMain/sourceToOutputs").get().asFile.mkdirs()
        layout.buildDirectory.dir("kspCaches/jvm/jvmMain/classpath").get().asFile.mkdirs()
    }
}
