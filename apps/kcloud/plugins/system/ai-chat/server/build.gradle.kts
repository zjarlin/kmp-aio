plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

val libs = versionCatalogs.named("libs")
val generatedApiOutputDir = rootDir
    .resolve("apps/kcloud/plugins/system/ai-chat/shared")
    .resolve("generated/commonMain/kotlin/site/addzero/kcloud/plugins/system/aichat/api")
    .absolutePath
val generatedJvmRouteSourceDir = layout.buildDirectory.dir("generated/ksp/jvm/jvmMain/kotlin")

dependencies {
    add("kspJvm", libs.findLibrary("org-babyfish-jimmer-jimmer-ksp").get())
    add("kspJvm", libs.findLibrary("spring2ktor-server-processor").get())
    add("kspJvm", libs.findLibrary("site-addzero-controller2api-processor").get())
}

ksp {
    arg("apiClientPackageName", "site.addzero.kcloud.plugins.system.aichat.api")
    arg("apiClientOutputDir", generatedApiOutputDir)
}

kotlin {
    sourceSets {
        jvmMain {
            kotlin.srcDir(generatedJvmRouteSourceDir)
        }
        jvmMain.dependencies {
            api(project(":apps:kcloud:plugins:system:ai-chat:shared"))
            implementation(project(":lib:ktor:plugin:ktor-jimmer-plugin"))
            implementation(libs.findLibrary("io-ktor-ktor-server-core-jvm").get())
            implementation(libs.findLibrary("org-babyfish-jimmer-jimmer-sql-kotlin").get())
            implementation(libs.findLibrary("spring2ktor-server-core").get())
            compileOnly(libs.findLibrary("org-springframework-spring-web").get())
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

tasks.register("generateRouteApis") {
    group = "code generation"
    description = "Generate Ktorfit APIs from Spring-style route sources."
    dependsOn("kspKotlinJvm")
}
