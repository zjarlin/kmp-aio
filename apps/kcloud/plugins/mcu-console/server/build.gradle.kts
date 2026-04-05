import site.addzero.ksp.modbusrtu.gradle.ModbusRtuExtension

plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.ksp.ksp-jvm-cache-preparation")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
    id("site.addzero.ksp.modbus-rtu")
}

val libs = versionCatalogs.named("libs")
val localAddzeroLibJvmDir = listOf(
    rootDir.resolve("../addzero-lib-jvm"),
    rootDir.resolve("addzero-lib-jvm"),
).firstOrNull { candidateDir -> candidateDir.resolve("settings.gradle.kts").isFile }
val localAddzeroLibJvmVersion = localAddzeroLibJvmDir
    ?.resolve("gradle.properties")
    ?.takeIf { file -> file.isFile }
    ?.readLines()
    ?.firstOrNull { line -> line.startsWith("version=") }
    ?.substringAfter("=")
    ?.trim()
    ?.takeIf(String::isNotBlank)
val addzeroLibJvmVersion = providers.gradleProperty("addzeroLibJvmVersion")
    .orElse(localAddzeroLibJvmVersion ?: "2026.04.04")
    .get()
val hasLocalAddzeroLibJvm = localAddzeroLibJvmDir != null
val localJimmerExternalProcessorPom = file(
    System.getProperty("user.home") +
        "/.m2/repository/site/addzero/jimmer-entity-external-processor/$addzeroLibJvmVersion/" +
        "jimmer-entity-external-processor-$addzeroLibJvmVersion.pom",
)
val generatedApiOutputDir = project(":apps:kcloud:plugins:mcu-console:ui")
    .projectDir
    .resolve("generated/commonMain/kotlin/site/addzero/kcloud/plugins/mcuconsole/api/external")
    .absolutePath
val generatedIsoPackage = "site.addzero.kcloud.plugins.mcuconsole"
val generatedIsoOutputDir = project(":apps:kcloud:plugins:mcu-console:shared")
    .projectDir
    .resolve("generated/commonMain/kotlin/site/addzero/kcloud/plugins/mcuconsole")
    .absolutePath
val sharedSourceDir = project(":apps:kcloud:plugins:mcu-console:shared")
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
    if (hasLocalAddzeroLibJvm) {
        add("kspJvm", project(":lib:ksp:metadata:jimmer-entity-external-processor"))
    } else if (localJimmerExternalProcessorPom.isFile) {
        add("kspJvm", "site.addzero:jimmer-entity-external-processor:$addzeroLibJvmVersion")
    }
    add("kspJvm", libs.findLibrary("spring2ktor-server-processor").get())
    add("kspJvm", libs.findLibrary("site-addzero-controller2api-processor").get())
}

ksp {
    arg("apiClientPackageName", "site.addzero.kcloud.plugins.mcuconsole.api.external")
    arg("apiClientOutputDir", generatedApiOutputDir)
    arg("sharedSourceDir", sharedSourceDir)
    arg("sharedComposeSourceDir", sharedComposeSourceDir)
    arg("backendServerSourceDir", projectDir.resolve("src/jvmMain/kotlin").absolutePath)
    arg("isomorphicPkg", generatedIsoPackage)
    arg("isomorphicGenDir", generatedIsoOutputDir)
    arg("isomorphicPackageName", generatedIsoPackage)
    arg("isomorphicClassSuffix", "Iso")
    arg("isomorphicSerializableEnabled", "true")
    arg("entity2Iso.enabled", "true")
    arg("entity2Form.enabled", "false")
    arg("entity2Mcp.enabled", "false")
    arg("formPackageName", "site.addzero.kcloud.plugins.mcuconsole.generated.forms")
    arg("enumOutputPackage", "site.addzero.kcloud.plugins.mcuconsole.generated.enums")
    arg("iso2DataProviderPackage", "site.addzero.kcloud.plugins.mcuconsole.generated.forms.dataprovider")
    arg("mcpPackageName", "site.addzero.kcloud.plugins.mcuconsole.generated.mcp")
}

configure<ModbusRtuExtension> {
    transports.set(listOf("rtu"))
    codegenModes.set(listOf("server"))
    contractPackages.set(
        listOf(
            "site.addzero.kcloud.plugins.mcuconsole.modbus.device",
            "site.addzero.kcloud.plugins.mcuconsole.modbus.atomic",
        ),
    )
}

kotlin {
    sourceSets {
        jvmMain {
            kotlin.srcDir(generatedJvmRouteSourceDir)
        }
        jvmMain.dependencies {
            api(project(":apps:kcloud:plugins:mcu-console:shared"))
            implementation(project(":lib:config-center"))
            implementation(project(":apps:kcloud:plugins:system:config-center"))
            implementation(project(":lib:tool-jvm:tool-stm32-bootloader"))
            implementation(project(":lib:tool-jvm:tool-serial"))
            implementation(project(":lib:ktor:plugin:ktor-jimmer-plugin"))
            implementation(libs.findLibrary("io-ktor-ktor-server-core-jvm").get())
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
