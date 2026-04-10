import org.gradle.api.tasks.Delete

plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-server-core")
}

val libs = versionCatalogs.named("libs")
val generateMcuConsoleContractsTask = ":apps:kcloud:plugins:codegen-context:server:generateMcuConsoleContracts"
val generateMcuConsoleContractsEnabled =
    providers
        .gradleProperty("generateMcuConsoleContracts")
        .map { !it.equals("false", ignoreCase = true) }
        .orElse(true)
        .get()
val mcuConsoleApiProject = project(":apps:kcloud:plugins:mcu-console:api")

/** Api生成目录 */
val generatedApiRootDir =
    mcuConsoleApiProject
        .layout
        .buildDirectory
        .dir("generated/source/controller2api/commonMain/kotlin")
val generatedApiPackageRootDirFile =
    generatedApiRootDir
        .get()
        .dir("site/addzero/kcloud/plugins/mcuconsole/api/external")
        .asFile
val generatedApiOutputDirFile =
    generatedApiPackageRootDirFile.resolve("generated")
val generatedApiOutputDir = generatedApiOutputDirFile.absolutePath

/** 同构体生成包 */
val generatedIsoPackage = "site.addzero.kcloud.plugins.mcuconsole"

/** 同构体生成目录 */
val generatedIsoOutputDir =
    project(":apps:kcloud:plugins:mcu-console:shared")
        .layout
        .buildDirectory
        .dir("generated/ksp/commonMain/kotlin/site/addzero/kcloud/plugins/mcuconsole")
        .get()
        .asFile
        .absolutePath

/** 共享目录 */
val sharedSourceDir = project(":apps:kcloud:plugins:mcu-console:shared").projectDir.resolve("src/commonMain/kotlin").absolutePath

/** 共享前端目录 */
val sharedComposeSourceDir = mcuConsoleApiProject.projectDir.resolve("src/commonMain/kotlin").absolutePath

/** 当前 server 源码目录 */
val backendServerSourceDir = projectDir.resolve("src/jvmMain/kotlin").absolutePath
val generatedContractSourceDir = layout.buildDirectory.dir("generated/source/codegen-context/jvmMain/kotlin")

ksp {
    arg("apiClientPackageName", "site.addzero.kcloud.plugins.mcuconsole.api.external.generated")
    arg("apiClientOutputDir", generatedApiOutputDir)
    arg("apiClientAggregatorOutputDir", generatedApiOutputDir)
    arg("sharedSourceDir", sharedSourceDir)
    arg("sharedComposeSourceDir", sharedComposeSourceDir)
    arg("backendServerSourceDir", backendServerSourceDir)

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

kotlin {
    sourceSets {
        jvmMain {
            kotlin.srcDir(generatedContractSourceDir)
            dependencies {
                implementation(project(":apps:kcloud:plugins:mcu-console:shared"))
                implementation(libs.findLibrary("modbus-runtime").get())
                implementation(libs.findLibrary("tool-stm32-bootloader").get())
                implementation(libs.findLibrary("tool-serial").get())
                implementation(project(":lib:ktor:plugin:ktor-jimmer-plugin"))
                implementation(project(":lib:ktor:starter:starter-spi"))
                implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-datetime").get())
            }
        }
        jvmTest.dependencies{
            implementation("io.github.jeadyx:kmp-serialport:1.0.0")
        }
    }
}

tasks.configureEach {
    when (name) {
        "compileKotlinJvm",
        "jvmSourcesJar",
        "jvmJar",
        -> {
            if (generateMcuConsoleContractsEnabled) {
                dependsOn(generateMcuConsoleContractsTask)
            }
        }
    }
}

val cleanMcuConsoleGeneratedApis by tasks.registering(Delete::class) {
    delete(generatedApiPackageRootDirFile)
}

tasks.matching { task ->
    task.name == "kspKotlinJvm"
}.configureEach {
    outputs.dir(generatedApiRootDir)
    dependsOn(cleanMcuConsoleGeneratedApis)
}
