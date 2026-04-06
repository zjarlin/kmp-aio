import site.addzero.ksp.modbusrtu.gradle.ModbusRtuExtension

plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-server-core")
    //rtu ksp生成
    id("site.addzero.ksp.modbus-rtu") version "+"
}

val libs = versionCatalogs.named("libs")

/** Api生成目录 */
val generatedApiOutputDir = project(":apps:kcloud:plugins:mcu-console:ui") .projectDir .resolve("generated/commonMain/kotlin/site/addzero/kcloud/plugins/mcuconsole/api/external") .absolutePath

/** 同构体生成目录 */
val generatedIsoPackage = "site.addzero.kcloud.plugins.mcuconsole"

/** 同构体生成包 */
val generatedIsoOutputDir = project(":apps:kcloud:plugins:mcu-console:shared") .projectDir .resolve("generated/commonMain/kotlin/site/addzero/kcloud/plugins/mcuconsole") .absolutePath

/** 共享目录 */
val sharedSourceDir = project(":apps:kcloud:plugins:mcu-console:shared") .projectDir .resolve("src/commonMain/kotlin") .absolutePath
/** 共享前端目录 */
val sharedComposeSourceDir = project(":apps:kcloud:plugins:mcu-console:ui") .projectDir .resolve("src/commonMain/kotlin") .absolutePath
//val generatedJvmRouteSourceDir = layout.buildDirectory.dir("generated/ksp/jvm/jvmMain/kotlin")

/** 当前server源码目录 */
val backendServerSourceDir = projectDir.resolve("src/jvmMain/kotlin").absolutePath

dependencies {
}

ksp {
    arg("apiClientPackageName", "site.addzero.kcloud.plugins.mcuconsole.api.external")
    arg("apiClientOutputDir", generatedApiOutputDir)
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
        jvmMain.dependencies {
            implementation(project(":apps:kcloud:plugins:mcu-console:shared"))
            implementation(libs.findLibrary("tool-stm32-bootloader").get())
            implementation(libs.findLibrary("tool-serial").get())
            implementation(project(":lib:ktor:plugin:ktor-jimmer-plugin"))
            implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-datetime").get())
        }
    }
}
