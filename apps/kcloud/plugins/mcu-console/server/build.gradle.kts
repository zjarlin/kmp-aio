plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-json")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

val libs = versionCatalogs.named("libs")
val localAddzeroLibJvmVersion = rootDir
    .resolve("../addzero-lib-jvm/gradle.properties")
    .takeIf { file -> file.isFile }
    ?.readLines()
    ?.firstOrNull { line -> line.startsWith("version=") }
    ?.substringAfter("=")
    ?.trim()
    ?.takeIf(String::isNotBlank)
    ?: "2026.10329.10127"
val localJimmerExternalProcessorPom = file(
    System.getProperty("user.home") +
        "/.m2/repository/site/addzero/jimmer-entity-external-processor/$localAddzeroLibJvmVersion/" +
        "jimmer-entity-external-processor-$localAddzeroLibJvmVersion.pom",
)
val generatedApiOutputDir = project(":apps:kcloud:plugins:mcu-console")
    .projectDir
    .resolve("generated/commonMain/kotlin/site/addzero/kcloud/plugins/mcuconsole/api/external")
    .absolutePath
val generatedIsoPackage = "site.addzero.kcloud.plugins.mcuconsole"
val generatedIsoOutputDir = project(":apps:kcloud:plugins:mcu-console")
    .projectDir
    .resolve("generated/commonMain/kotlin/site/addzero/kcloud/plugins/mcuconsole")
    .absolutePath
val sharedSourceDir = project(":apps:kcloud:plugins:mcu-console")
    .projectDir
    .resolve("src/commonMain/kotlin")
    .absolutePath
val sharedComposeSourceDir = project(":apps:kcloud:plugins:mcu-console:ui")
    .projectDir
    .resolve("src/commonMain/kotlin")
    .absolutePath
val generatedJvmRouteSourceDir = layout.buildDirectory.dir("generated/ksp/jvm/jvmMain/kotlin")

dependencies {
    add("kspJvm", libs.findLibrary("org-babyfish-jimmer-jimmer-ksp").get())
    add("kspJvm", project(":lib:ksp:metadata:modbus:modbus-ksp-rtu"))
    if (localJimmerExternalProcessorPom.isFile) {
        add("kspJvm", "site.addzero:jimmer-entity-external-processor:$localAddzeroLibJvmVersion")
    }
    add("kspJvm", libs.findLibrary("spring2ktor-server-processor").get())
    add("kspJvm", libs.findLibrary("site-addzero-controller2api-processor").get())
}

ksp {
    arg("addzero.modbus.codegen.mode", "server")
    arg("addzero.modbus.contractPackages", "site.addzero.kcloud.plugins.mcuconsole.service.modbus")
    arg("apiClientPackageName", "site.addzero.kcloud.plugins.mcuconsole.api.external")
    arg("apiClientOutputDir", generatedApiOutputDir)
    arg("sharedSourceDir", sharedSourceDir)
    arg("sharedComposeSourceDir", sharedComposeSourceDir)
    arg("backendServerSourceDir", projectDir.resolve("src/jvmMain/kotlin").absolutePath)
    arg("isomorphicPkg", generatedIsoPackage)
    arg("isomorphicGenDir", generatedIsoOutputDir)
    arg("isomorphicPackageName", generatedIsoPackage)
    arg("isomorphicClassSuffix", "Iso")
    arg("entity2Iso.enabled", "true")
    arg("entity2Form.enabled", "false")
    arg("entity2Mcp.enabled", "false")
    arg("formPackageName", "site.addzero.kcloud.plugins.mcuconsole.generated.forms")
    arg("enumOutputPackage", "site.addzero.kcloud.plugins.mcuconsole.generated.enums")
    arg("iso2DataProviderPackage", "site.addzero.kcloud.plugins.mcuconsole.generated.forms.dataprovider")
    arg("mcpPackageName", "site.addzero.kcloud.plugins.mcuconsole.generated.mcp")
}

kotlin {
    sourceSets {
        jvmMain {
            kotlin.srcDir(generatedJvmRouteSourceDir)
        }
        jvmMain.dependencies {
            implementation(project(":apps:kcloud:plugins:mcu-console"))
            implementation(project(":lib:ksp:metadata:modbus:modbus-runtime"))
            implementation(project(":lib:ktor:plugin:ktor-jimmer-plugin"))
            implementation(libs.findLibrary("com-hivemq-hivemq-mqtt-client").get())
            implementation(libs.findLibrary("io-ktor-ktor-server-core-jvm").get())
            implementation(libs.findLibrary("j2mod").get())
            implementation(libs.findLibrary("jserialcomm").get())
            implementation(libs.findLibrary("org-babyfish-jimmer-jimmer-sql-kotlin").get())
            implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-datetime").get())
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
