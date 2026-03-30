plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-json")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

val libs = versionCatalogs.named("libs")
val generatedApiOutputDir = project(":apps:kcloud:plugins:mcu-console")
    .projectDir
    .resolve("generated/commonMain/kotlin/site/addzero/kcloud/plugins/mcuconsole/api/external")
    .absolutePath
val generatedJvmRouteSourceDir = layout.buildDirectory.dir("generated/ksp/jvm/jvmMain/kotlin")

dependencies {
    add("kspJvm", project(":lib:ksp:metadata:modbus:modbus-ksp-rtu"))
    add("kspJvm", libs.findLibrary("spring2ktor-server-processor").get())
    add("kspJvm", libs.findLibrary("site-addzero-controller2api-processor").get())
}

ksp {
    arg("addzero.modbus.codegen.mode", "server")
    arg("addzero.modbus.contractPackages", "site.addzero.kcloud.plugins.mcuconsole.service.modbus")
    arg("apiClientPackageName", "site.addzero.kcloud.plugins.mcuconsole.api.external")
    arg("apiClientOutputDir", generatedApiOutputDir)
}

kotlin {
    sourceSets {
        jvmMain {
            kotlin.srcDir(generatedJvmRouteSourceDir)
        }
        jvmMain.dependencies {
            implementation(project(":apps:kcloud:plugins:mcu-console"))
            implementation(project(":lib:ksp:metadata:modbus:modbus-runtime"))
            implementation(libs.findLibrary("com-hivemq-hivemq-mqtt-client").get())
            implementation(libs.findLibrary("io-ktor-ktor-server-core-jvm").get())
            implementation(libs.findLibrary("jserialcomm").get())
            implementation(libs.findLibrary("spring2ktor-server-core").get())
            compileOnly(libs.findLibrary("org-springframework-spring-web").get())
        }
    }
}

tasks.register("generateRouteApis") {
    group = "code generation"
    description = "Generate Ktorfit APIs from Spring-style route sources."
    dependsOn("kspKotlinJvm")
}
